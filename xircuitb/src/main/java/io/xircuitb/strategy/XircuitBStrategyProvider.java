package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategy;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.exception.XircuitBConfigurationException;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.factory.XircuitBNameFactory;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.monitor.XircuitBMonitor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.resilix.util.ResiliXUtils.fmt;
import static io.xircuitb.validator.XircuitBValidator.validateParameters;

@Slf4j
@Component
@RequiredArgsConstructor
public class XircuitBStrategyProvider implements ResiliXStrategy<XircuitB, XircuitBConfigModel, XircuitBCacheModel> {

    private final Clock clock;
    private final XircuitBConfigFactory configFactory;
    private final XircuitBNameFactory nameFactory;
    private final CircuitBreakerRegistry registry;
    private final XircuitBMonitor monitor;

    private final ConcurrentMap<String, XircuitBCacheModel> xbCache = new ConcurrentHashMap<>();

    @Value("${resilix.xircuitb.priority:1}")
    protected int priority;

    @Override
    public Class<XircuitB> support() {
        return XircuitB.class;
    }

    @Override
    public int priority() {
        return priority;
    }

    @Override
    public XircuitBCacheModel computeCache(String xbName, XircuitB xb, ResiliXContext ctx) {
        return xbCache.computeIfAbsent(xbName, name -> {
            XircuitBConfigModel config = createConfiguration(xb, ctx);
            CircuitBreaker cb = createCircuitBreaker(config, name);
            return cb == null ? null : new XircuitBCacheModel(cb, config);
        });
    }

    @Override
    public XircuitBConfigModel createConfiguration(XircuitB xb, ResiliXContext ctx) {
        try {
            XircuitBConfigModel config = configFactory.resolveConfig(xb, ctx);
            validateParameters(config);
            return config;
        } catch (Exception e) {
            throw new XircuitBConfigurationException(fmt("Failed to resolve configuration for %s", ctx.getMethod().getName()), e);
        }
    }

    @Override
    public String resolveName(XircuitB xb, ResiliXContext ctx, int index) {
        return nameFactory.resolveName(xb, ctx, index);
    }

    protected CircuitBreaker createCircuitBreaker(XircuitBConfigModel resolvedConfig, String xbName) {
        if (resolvedConfig == null) return null;
        try {
            CircuitBreaker cb = registry.circuitBreaker(xbName, () -> configFactory.buildCircuitBreakerConfig(resolvedConfig, clock));
            monitor.logInitParam(cb, resolvedConfig);
            return cb;
        } catch (Exception e) {
            throw new XircuitBConfigurationException(e.getMessage(), e);
        }
    }

    protected Clock getClock() {
        return clock;
    }

}
