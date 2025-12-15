package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategyAsync;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.factory.XircuitBNameFactory;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.monitor.XircuitBMonitor;
import io.xircuitb.provider.XircuitBFallbackProviderAsync;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static io.resilix.util.ResiliXUtils.unwrap;

@Component
public class XircuitBStrategyProviderAsync extends XircuitBStrategyProvider implements ResiliXStrategyAsync<XircuitB, XircuitBConfigModel, XircuitBCacheModel> {

    @Autowired
    public XircuitBStrategyProviderAsync(Clock clock, XircuitBConfigFactory configFactory, XircuitBNameFactory nameFactory, CircuitBreakerRegistry registry, XircuitBMonitor monitor) {
        super(clock, configFactory, nameFactory, registry, monitor);
    }

    @Override
    public Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> supplier, ResiliXContext ctx) {
        return applyCircuitBreakersAsync(extractAnnotations(ctx, XircuitB.class), supplier, ctx);
    }

    private Supplier<CompletionStage<Object>> applyCircuitBreakersAsync(List<XircuitB> xbs, Supplier<CompletionStage<Object>> supplier, ResiliXContext ctx) {
        Supplier<CompletionStage<Object>> wrapped = supplier;
        int index = 0;

        for (XircuitB xb : xbs) {
            String xbName = resolveName(xb, ctx, ++index);
            XircuitBCacheModel cache = computeCache(xbName, xb, ctx);

            if (cache != null && cache.config().getActiveSchedule().isActiveNow(getClock())) {
                wrapped = wrapAsync(
                        cache.cb(),
                        wrapped,
                        cache.config().getFallbackProvider() instanceof XircuitBFallbackProviderAsync fallback ? fallback : null);
            }
        }

        return wrapped;
    }

    private Supplier<CompletionStage<Object>> wrapAsync(CircuitBreaker cb, Supplier<CompletionStage<Object>> supplier, XircuitBFallbackProviderAsync fallbackProvider) {
        return () -> cb.executeCompletionStage(supplier)
                .exceptionallyCompose(ex -> {
                    Throwable cause = unwrap(ex);
                    if (cause instanceof CallNotPermittedException cnp && fallbackProvider != null) {
                        return fallbackProvider.returnFallbackModel(
                                cb.getName(),
                                fallbackProvider.apply(cnp),
                                cnp
                        );
                    }
                    CompletableFuture<Object> failed = new CompletableFuture<>();
                    failed.completeExceptionally(cause);
                    return failed;
                });
    }

}
