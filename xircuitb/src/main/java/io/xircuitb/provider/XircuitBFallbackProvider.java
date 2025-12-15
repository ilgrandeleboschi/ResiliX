package io.xircuitb.provider;

import io.resilix.provider.ResiliXFallbackProvider;

/**
 * Internal base interface for fallback providers.
 * Do not implement directly. Implement only Sync or Async versions.
 */
public interface XircuitBFallbackProvider extends ResiliXFallbackProvider {

    @Override
    default void log(String xbName, Throwable cause) {
        getLogger().warn("Circuit open for xb {}", xbName, cause);
    }

}
