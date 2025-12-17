package io.xircuitb.model;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;

public record XircuitBCacheModel(CircuitBreaker cb, XircuitBConfigModel config) {
}
