package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface ResiliXStrategy {

    boolean support(Annotation annotation);

    int priority();

    default CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, Method method) {
        return execution;
    }

    default Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> execution, Method method) {
        return execution;
    }

}
