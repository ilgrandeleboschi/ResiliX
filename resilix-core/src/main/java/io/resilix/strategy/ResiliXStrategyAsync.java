package io.resilix.strategy;

import io.resilix.model.ResiliXContext;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public interface ResiliXStrategyAsync extends ResiliXStrategy {

    @Override
    Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> execution, ResiliXContext ctx);

}
