package io.resilix.pipeline;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.strategy.ResiliXStrategy;
import org.aspectj.lang.ProceedingJoinPoint;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static io.resilix.utils.ResiliXUtils.isAsync;
import static io.resilix.utils.ResiliXUtils.wrapAsync;

public record ResiliXExecutionPipeline(List<? extends ResiliXStrategy> strategies) {

    public Object execute(ProceedingJoinPoint pjp, Method method) throws Throwable {
        if (isAsync(method)) {
            Supplier<CompletionStage<Object>> chain = wrapAsync(pjp);
            for (int i = strategies.size() - 1; i >= 0; i--) {
                chain = strategies.get(i).decorate(chain, method);
            }
            return chain.get();
        } else {
            CheckedSupplier<Object> chain = pjp::proceed;
            for (int i = strategies.size() - 1; i >= 0; i--) {
                chain = strategies.get(i).decorate(chain, method);
            }
            return chain.get();
        }
    }
}