package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.xircuitb.provider.defaults.VoidFallbackProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;


class VoidFallbackProviderTest {

    @Test
    void apply() {
        CallNotPermittedException ex = mock(CallNotPermittedException.class);
        assertNull(new VoidFallbackProvider().apply(ex));
    }

}