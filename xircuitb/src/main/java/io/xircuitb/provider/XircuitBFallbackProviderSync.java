package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

@FunctionalInterface
public interface XircuitBFallbackProviderSync extends XircuitBFallbackProvider {

    Object apply(CallNotPermittedException cause);

    default Object returnFallbackModel(String xbName, Object model, CallNotPermittedException cause) {
        logOperCircuit(xbName, cause);
        return model;
    }

}
