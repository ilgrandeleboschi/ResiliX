package io.xircuitb.provider.defaults;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.xircuitb.provider.XircuitBFallbackProvider;

public class VoidFallbackProvider implements XircuitBFallbackProvider {
    @Override
    public Object apply(CallNotPermittedException cause) {
        return null;
    }
}
