package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

public class VoidFallbackProvider implements XircuitBFallbackProvider {
    @Override
    public Object apply(CallNotPermittedException cause) {
        return null;
    }
}
