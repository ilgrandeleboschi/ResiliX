package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@FunctionalInterface
public interface XircuitBFallbackProvider {

    Object apply(CallNotPermittedException cause);

    default void logOperCircuit(String xbName, CallNotPermittedException cause) {
        getLogger().warn("Circuit open for xb {}", xbName, cause);
    }

    default <T> T returnFallbackModel(String xbName, T model, CallNotPermittedException cause) {
        logOperCircuit(xbName, cause);
        return model;
    }

    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
