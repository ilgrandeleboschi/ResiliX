package utils;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.xircuitb.provider.XircuitBFallbackProviderAsync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class MockFallbackProviderAsync implements XircuitBFallbackProviderAsync {
    @Override
    public CompletionStage<Object> apply(CallNotPermittedException cause) {
        return CompletableFuture.completedFuture("Fallback executed");
    }
}
