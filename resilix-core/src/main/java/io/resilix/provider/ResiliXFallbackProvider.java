package io.resilix.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Internal base interface for fallback providers.
 * Do not implement directly. Implement only Sync or Async versions.
 */
@FunctionalInterface
public interface ResiliXFallbackProvider {

    void log(String name, Throwable cause);

    default Logger getLogger() {
        return LoggerFactory.getLogger(this.getClass());
    }

}
