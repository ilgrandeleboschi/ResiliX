package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;

import java.lang.reflect.Method;

public interface ResiliXStrategySync extends ResiliXStrategy {

    @Override
    CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, Method method);

}
