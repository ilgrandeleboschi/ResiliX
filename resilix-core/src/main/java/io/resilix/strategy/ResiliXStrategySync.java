package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;

public interface ResiliXStrategySync extends ResiliXStrategy {

    @Override
    CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, ResiliXContext ctx);

}
