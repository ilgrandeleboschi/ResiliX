package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.resilix.strategy.ResiliXStrategy;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.annotation.XircuitBs;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.ActivePeriod;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.model.XircuitBConfigModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.xircuitb.validator.XircuitBValidator.validateParameters;

@Slf4j
@Component
@RequiredArgsConstructor
public class XircuitBStrategyProvider implements ResiliXStrategy {

    protected final CircuitBreakerRegistry registry;
    protected final XircuitBConfigFactory factory;
    protected final Clock clock;

    protected final ConcurrentMap<String, XircuitBCacheModel> xbCache = new ConcurrentHashMap<>();

    @Value("${resilix.xircuitb.priority:1}")
    protected int priority;

    @Override
    public boolean support(Annotation annotation) {
        return annotation instanceof XircuitB || annotation instanceof XircuitBs;
    }

    @Override
    public int priority() {
        return priority;
    }

    protected String getXbName(Method method, XircuitB xB, int index) {
        return factory.resolveXbName(method, xB, index);
    }

    protected XircuitB[] getXBS(Method method) {
        return method.getAnnotation(XircuitBs.class) != null
                ? method.getAnnotation(XircuitBs.class).value()
                : method.getAnnotationsByType(XircuitB.class);
    }

    protected XircuitBCacheModel computeCache(String xbName, XircuitB xB) {
        return xbCache.computeIfAbsent(xbName, name -> {
            XircuitBConfigModel config = createConfiguration(xB, name);
            CircuitBreaker cb = createCircuitBreaker(config, name);
            return cb == null ? null : new XircuitBCacheModel(cb, config);
        });
    }

    protected CircuitBreaker createCircuitBreaker(XircuitBConfigModel resolvedConfig, String xbName) {
        try {
            return resolvedConfig == null ? null : registry.circuitBreaker(xbName, () -> factory.buildCircuitBreakerConfig(resolvedConfig));
        } catch (Exception e) {
            log.warn("Failed to create CircuitBreaker '{}', executing normally: {}", xbName, e.getMessage(), e);
            return null;
        }
    }

    protected XircuitBConfigModel createConfiguration(XircuitB xB, String xbName) {
        try {
            XircuitBConfigModel config = factory.resolveConfig(xB);
            validateParameters(config);
            return config;
        } catch (Exception e) {
            log.warn("Failed to create configuration for '{}', executing normally: {}", xbName, e.getMessage(), e);
            return null;
        }
    }

    protected boolean isActiveNow(XircuitBConfigModel config) {
        return new ActivePeriod(
                config.getActiveFrom(),
                config.getActiveTo(),
                List.of(config.getActiveDays() == null ? DayOfWeek.values() : config.getActiveDays())
        ).isActiveNow(clock);
    }
}
