package io.resilix.pipeline;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;
import org.aspectj.lang.ProceedingJoinPoint;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static io.resilix.utils.ResiliXUtils.isAsync;
import static io.resilix.utils.ResiliXUtils.proceedSupplier;
import static io.resilix.utils.ResiliXUtils.wrapAsync;

public record ResiliXExecutionPipeline(ResiliXStrategyPipeline strategy) {

    public Object execute(ProceedingJoinPoint pjp, ResiliXContext ctx) throws Throwable {
        strategy.onEnter(ctx);
        try {
            Object result;
            if (isAsync(ctx.getMethod())) {
                Supplier<CompletionStage<Object>> chain = strategy.decorate(wrapAsync(pjp), ctx);
                result = chain.get();
            } else {
                CheckedSupplier<Object> chain = strategy.decorate(proceedSupplier(pjp), ctx);
                result = chain.get();
            }
            strategy.onSuccess(result, ctx);
            return result;
        } catch (Throwable t) {
            strategy.onError(t, ctx);
            throw t;
        } finally {
            strategy.onExit(ctx);
        }
    }

}