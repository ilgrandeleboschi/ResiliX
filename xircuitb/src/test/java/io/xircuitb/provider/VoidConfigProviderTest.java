package io.xircuitb.provider;

import io.xircuitb.provider.defaults.VoidConfigProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class VoidConfigProviderTest {

    @Test
    void apply() {
        assertNull(new VoidConfigProvider().apply());
    }

}