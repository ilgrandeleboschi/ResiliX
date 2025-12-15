package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategySync;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.factory.XircuitBNameFactory;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.monitor.XircuitBMonitor;
import io.xircuitb.provider.XircuitBFallbackProviderSync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;

@Component
public class XircuitBStrategyProviderSync extends XircuitBStrategyProvider implements ResiliXStrategySync<XircuitB, XircuitBConfigModel, XircuitBCacheModel> {

    @Autowired
    public XircuitBStrategyProviderSync(Clock clock, XircuitBConfigFactory configFactory, XircuitBNameFactory nameFactory, CircuitBreakerRegistry registry, XircuitBMonitor monitor) {
        super(clock, configFactory, nameFactory, registry, monitor);
    }

    @Override
    public CheckedSupplier<Object> decorate(CheckedSupplier<Object> checkedSupplier, ResiliXContext ctx) {
        return applyCircuitBreakersSync(extractAnnotations(ctx, XircuitB.class), checkedSupplier, ctx);
    }

    private CheckedSupplier<Object> applyCircuitBreakersSync(List<XircuitB> xbs, CheckedSupplier<Object> execution, ResiliXContext ctx) {
        CheckedSupplier<Object> wrapped = execution;
        int index = 0;

        for (XircuitB xb : xbs) {
            String xbName = resolveName(xb, ctx, ++index);
            XircuitBCacheModel cache = computeCache(xbName, xb, ctx);

            if (cache != null && cache.config().getActiveSchedule().isActiveNow(getClock())) {
                wrapped = wrap(
                        cache.cb(),
                        wrapped,
                        cache.config().getFallbackProvider() instanceof XircuitBFallbackProviderSync fallback ? fallback : null);
            }

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
