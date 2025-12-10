package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XircuitBFallbackProviderAsyncTest {

    @Mock
    CallNotPermittedException ex;

    static class XircuitBFallbackProviderAsyncDummy implements XircuitBFallbackProviderAsync {
        @Override
        public CompletionStage<Object> apply(CallNotPermittedException cause) {
            return null;
        }
    }

    @Test
    void applyTest() {
        XircuitBFallbackProviderAsyncDummy dummy = new XircuitBFallbackProviderAsyncDummy();

        CompletionStage<Object> completionStage = CompletableFuture.completedStage("fallback model");

        assertNull(dummy.apply(ex));
        assertEquals(completionStage, dummy.returnFallbackModel("", completionStage, ex));
    }

}