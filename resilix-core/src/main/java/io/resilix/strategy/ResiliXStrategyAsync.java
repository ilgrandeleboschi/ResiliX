package io.resilix.strategy;

import io.resilix.model.ResiliXCacheModel;
import io.resilix.model.ResiliXConfigModel;
import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface ResiliXStrategyAsync<A extends Annotation, C extends ResiliXConfigModel, K extends ResiliXCacheModel<C>> extends ResiliXStrategy<A, C, K> {

    @Override
    Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> execution, ResiliXContext ctx);

}
