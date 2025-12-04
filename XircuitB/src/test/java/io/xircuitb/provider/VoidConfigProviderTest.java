package io.xircuitb.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

class VoidConfigProviderTest {

    @Test
    void apply() {
        assertNull(new VoidConfigProvider().apply());
    }

}