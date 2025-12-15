package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;

public interface ResiliXStrategySync<A extends Annotation, C, K> extends ResiliXStrategy<A, C, K> {

    @Override
    CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, ResiliXContext ctx);

}
