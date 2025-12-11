package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface ResiliXStrategy {

    boolean support(Annotation annotation);

    int priority();

    default CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, ResiliXContext ctx) {
        return execution;
    }

    default Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> execution, ResiliXContext ctx) {
        return execution;
    }

    default void onEnter(ResiliXContext ctx) {
    }

    default void onSuccess(Object result, ResiliXContext ctx) {
    }

    default void onError(Throwable error, ResiliXContext ctx) {
    }

    default void onExit(ResiliXContext ctx) {
    }

}
