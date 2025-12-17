package io.xircuitb.model;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.resilix.model.ResiliXCacheModel;

public record XircuitBCacheModel(CircuitBreaker cb,
                                 XircuitBConfigModel config) implements ResiliXCacheModel<XircuitBConfigModel> {
}
