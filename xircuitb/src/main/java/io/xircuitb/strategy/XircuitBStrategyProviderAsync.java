package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.resilix.strategy.ResiliXStrategyAsync;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.provider.XircuitBFallbackProviderAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@Component
public class XircuitBStrategyProviderAsync extends XircuitBStrategyProvider implements ResiliXStrategyAsync {

    @Autowired
    public XircuitBStrategyProviderAsync(CircuitBreakerRegistry registry, XircuitBConfigFactory factory, Clock clock) {
        super(registry, factory, clock);
    }

    @Override
    public Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> supplier, Method method) {
        return applyCircuitBreakersAsync(getXBS(method), method, supplier);
    }

    private Supplier<CompletionStage<Object>> applyCircuitBreakersAsync(XircuitB[] xBs, Method method, Supplier<CompletionStage<Object>> supplier) {
        Supplier<CompletionStage<Object>> wrapped = supplier;

        for (int i = 0; i < xBs.length; i++) {
            XircuitB xB = xBs[i];
            String xbName = getXbName(method, xB, i + 1);
            XircuitBCacheModel cache = computeCache(xbName, xB);

            if (cache != null && isActiveNow(cache.getConfig())) {
                wrapped = wrapAsync(cache.getCb(), wrapped, factory.resolveAsyncFallback(xB.fallbackProvider()));
            }
        }

        return wrapped;
    }

    private Supplier<CompletionStage<Object>> wrapAsync(CircuitBreaker cb, Supplier<CompletionStage<Object>> supplier, XircuitBFallbackProviderAsync fallbackProvider) {
        return () -> cb.executeCompletionStage(supplier)
                .exceptionallyCompose(ex -> {
                    if (ex instanceof CallNotPermittedException e && fallbackProvider != null) {
                        return fallbackProvider.returnFallbackModel(cb.getName(), fallbackProvider.apply(e), e);
                    }
                    CompletableFuture<Object> failed = new CompletableFuture<>();
                    failed.completeExceptionally(ex);
                    return failed;
                });
    }

}
