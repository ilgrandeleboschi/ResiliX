package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface ResiliXStrategy<A extends Annotation, C, K> {

    Class<A> support();

    int priority();

    C createConfiguration(A annotation, ResiliXContext ctx);

    K computeCache(String key, A annotation, ResiliXContext ctx);

    String resolveName(A annotation, ResiliXContext ctx, int index);

    default List<A> extractAnnotations(ResiliXContext ctx, Class<A> clazz) {
        return List.of(ctx.getMethod().getAnnotationsByType(clazz));
    }

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
