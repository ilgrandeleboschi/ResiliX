package io.resilix.pipeline;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static io.resilix.util.ResiliXUtils.isAsync;
import static io.resilix.util.ResiliXUtils.proceedSupplier;
import static io.resilix.util.ResiliXUtils.wrapAsync;

public record ResiliXExecutionPipeline<A extends Annotation, C, K>(ResiliXStrategyPipeline<A, C, K> strategy) {

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