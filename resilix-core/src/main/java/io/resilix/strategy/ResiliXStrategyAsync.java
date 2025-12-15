package io.resilix.strategy;

import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface ResiliXStrategyAsync<A extends Annotation, C, K> extends ResiliXStrategy<A, C, K> {

    @Override
    Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> execution, ResiliXContext ctx);

}
