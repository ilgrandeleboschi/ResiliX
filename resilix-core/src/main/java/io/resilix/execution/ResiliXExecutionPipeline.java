package io.resilix.execution;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.provider.ResiliXStrategy;

import java.util.List;

public record ResiliXExecutionPipeline(List<ResiliXStrategy> strategies) {

    public Object execute(CheckedSupplier<Object> original) throws Throwable {
        CheckedSupplier<Object> chain = original;

        for (int i = strategies.size() - 1; i >= 0; i--) {
            chain = strategies.get(i).apply(chain);
        }

        return chain.get();
    }

}
