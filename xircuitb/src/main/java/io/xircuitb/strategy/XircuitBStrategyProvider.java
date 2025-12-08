package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.provider.ResiliXStrategy;
import io.resilix.provider.ResiliXStrategyProvider;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.annotation.XircuitBs;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.ActivePeriod;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.provider.XircuitBFallbackProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
@ConfigurationProperties(prefix = "resilix.xircuitb")
public class XircuitBStrategyProvider implements ResiliXStrategyProvider {

    private final CircuitBreakerRegistry registry;
    private final XircuitBConfigFactory factory;
    private final Clock clock;

    private final ConcurrentMap<String, XircuitBCacheModel> xbCache = new ConcurrentHashMap<>();

    private final int priority;

    @Override
    public ResiliXStrategy strategy(Annotation annotation, Method method) {
        XircuitB[] xBs = method.getAnnotation(XircuitBs.class) != null ?
                method.getAnnotation(XircuitBs.class).value() :
                method.getAnnotationsByType(XircuitB.class);
        return execution -> applyCircuitBreakers(xBs, method, execution);
    }

    @Override
    public boolean support(Annotation annotation) {
        return annotation instanceof XircuitB || annotation instanceof XircuitBs;
    }

    @Override
    public int priority() {
        return priority;
    }

    private CheckedSupplier<Object> applyCircuitBreakers(XircuitB[] xBs, Method method, CheckedSupplier<Object> execution) {
        int index = 0;
        for (XircuitB xB : xBs) {
            String xbName = factory.resolveXbName(method, xB, ++index);

            XircuitBCacheModel cache = xbCache.computeIfAbsent(xbName, name -> {
                XircuitBConfigModel resolvedConfig = createConfiguration(xB, name);
                CircuitBreaker cb = createCircuitBreaker(resolvedConfig, name);
                return cb == null ? null : new XircuitBCacheModel(cb, resolvedConfig);
            });

            if (cache != null && isActiveNow(cache.getConfig())) {
                execution = wrapWithCircuitBreaker(cache.getCb(), execution, factory.resolveFallback(xB.fallbackProvider()));
            }
        }
        return execution;
    }

    private CheckedSupplier<Object> wrapWithCircuitBreaker(CircuitBreaker cb, CheckedSupplier<Object> execution, XircuitBFallbackProvider fallbackProvider) {
        return () -> {
            try {
                return cb.executeCheckedSupplier(execution);
            } catch (CallNotPermittedException e) {
                if (fallbackProvider != null) {
                    Object ret = fallbackProvider.apply(e);
                    return fallbackProvider.returnFallbackModel(cb.getName(), ret, e);
                }
                throw e;
            }
        };
    }

    private CircuitBreaker createCircuitBreaker(XircuitBConfigModel resolvedConfig, String xbName) {
        try {
            return resolvedConfig == null ? null : registry.circuitBreaker(xbName, () -> factory.buildCircuitBreakerConfig(resolvedConfig));
        } catch (Exception e) {
            log.warn("Failed to create CircuitBreaker '{}', skipping creation and executing method normally: {}", xbName, e.getMessage(), e);
            return null;
        }
    }

    private XircuitBConfigModel createConfiguration(XircuitB xB, String xbName) {
        try {
            XircuitBConfigModel resolvedConfig = factory.resolveConfig(xB);
            validateParameters(resolvedConfig);
            return resolvedConfig;
        } catch (Exception e) {
            log.warn("Failed to create configuration for CircuitBreaker '{}', skipping creation and executing method normally: {}", xbName, e.getMessage(), e);
            return null;
        }
    }

    private boolean isActiveNow(XircuitBConfigModel resolvedConfig) {
        return new ActivePeriod(resolvedConfig.getActiveFrom(), resolvedConfig.getActiveTo(), List.of(resolvedConfig.getActiveDays() == null ? DayOfWeek.values() : resolvedConfig.getActiveDays())).isActiveNow(clock);
    }

}
