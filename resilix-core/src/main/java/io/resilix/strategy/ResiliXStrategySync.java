package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXCacheModel;
import io.resilix.model.ResiliXConfigModel;
import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;

public interface ResiliXStrategySync<A extends Annotation, C extends ResiliXConfigModel, K extends ResiliXCacheModel<C>> extends ResiliXStrategy<A, C, K> {

    @Override
    CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, ResiliXContext ctx);

}
