package io.xircuitb.model;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class XircuitBCacheModel {

    private CircuitBreaker cb;
    private XircuitBConfigModel config;

}
