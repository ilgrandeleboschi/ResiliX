package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.strategy.ResiliXStrategySync;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.provider.XircuitBFallbackProviderSync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Clock;

@Component
public class XircuitBStrategyProviderSync extends XircuitBStrategyProvider implements ResiliXStrategySync {

    @Autowired
    public XircuitBStrategyProviderSync(CircuitBreakerRegistry registry, XircuitBConfigFactory factory, Clock clock) {
        super(registry, factory, clock);
    }

    @Override
    public CheckedSupplier<Object> decorate(CheckedSupplier<Object> checkedSupplier, Method method) {
        return applyCircuitBreakersSync(getXBS(method), method, checkedSupplier);
    }

    private CheckedSupplier<Object> applyCircuitBreakersSync(XircuitB[] xBs, Method method, CheckedSupplier<Object> execution) {
        CheckedSupplier<Object> wrapped = execution;

        for (int i = 0; i < xBs.length; i++) {
            XircuitB xB = xBs[i];
            String xbName = getXbName(method, xB, i + 1);
            XircuitBCacheModel cache = computeCache(xbName, xB);

            if (cache != null && isActiveNow(cache.getConfig()))
                wrapped = wrap(cache.getCb(), wrapped, factory.resolveSyncFallback(xB.fallbackProvider()));

        }

        return wrapped;
    }

    private CheckedSupplier<Object> wrap(CircuitBreaker cb, CheckedSupplier<Object> execution, XircuitBFallbackProviderSync fallbackProvider) {
        return () -> {
            try {
                return cb.executeCheckedSupplier(execution);
            } catch (CallNotPermittedException e) {
                if (fallbackProvider != null)
                    return fallbackProvider.returnFallbackModel(cb.getName(), fallbackProvider.apply(e), e);
                throw e;
            }
        };
    }

}
