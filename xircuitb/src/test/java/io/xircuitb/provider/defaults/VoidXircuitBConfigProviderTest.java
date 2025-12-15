package io.xircuitb.provider.defaults;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class VoidXircuitBConfigProviderTest {

    @Test
    void getTest() {
        assertNull(new VoidXircuitBConfigProvider().get());
    }

    @Test
    void nameTest() {
        assertEquals("", new VoidXircuitBConfigProvider().name());
    }

}