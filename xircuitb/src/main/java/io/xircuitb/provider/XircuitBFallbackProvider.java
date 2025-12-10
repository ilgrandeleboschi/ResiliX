package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal base interface for fallback providers.
 * Do not implement directly. Implement only Sync or Async versions.
 */
public interface XircuitBFallbackProvider {

    default void logOperCircuit(String xbName, CallNotPermittedException cause) {
        getLogger().warn("Circuit open for xb {}", xbName, cause);
    }

    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
