package io.resilix.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResiliXFallbackProviderTest {

    static class MockResiliXFallbackProvider implements ResiliXFallbackProvider {
        @Override
        public void log(String name, Throwable cause) {
            // test
        }
    }

    @Test
    void getLoggerTest() {
        assertEquals("io.resilix.provider.ResiliXFallbackProviderTest$MockResiliXFallbackProvider", new MockResiliXFallbackProvider().getLogger().getName());
    }

}