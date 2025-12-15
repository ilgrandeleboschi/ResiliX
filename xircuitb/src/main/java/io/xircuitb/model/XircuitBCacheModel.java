package io.xircuitb.model;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.resilix.model.ResiliXContext;

public record XircuitBCacheModel(CircuitBreaker cb, XircuitBConfigModel config, ResiliXContext ctx) {
}
