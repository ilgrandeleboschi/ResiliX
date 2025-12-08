package io.resilix.provider;

import io.github.resilience4j.core.functions.CheckedSupplier;

@FunctionalInterface
public interface ResiliXStrategy {

    CheckedSupplier<Object> apply(CheckedSupplier<Object> execution);

}
