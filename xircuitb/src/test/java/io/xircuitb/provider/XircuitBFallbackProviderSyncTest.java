package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class XircuitBFallbackProviderSyncTest {

    @Mock
    CallNotPermittedException ex;

    static class XircuitBFallbackProviderSyncDummy implements XircuitBFallbackProviderSync {
        @Override
        public Object apply(CallNotPermittedException cause) {
            return null;
        }
    }

    @Test
    void applyTest() {
        XircuitBFallbackProviderSyncDummy dummy = new XircuitBFallbackProviderSyncDummy();

        assertNull(dummy.apply(ex));
        assertEquals("fallback model", dummy.returnFallbackModel("", "fallback model", ex));
    }

}