package io.resilix.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class ResiliXExceptionTest {

    @Test
    void exTest() {
        ResiliXException re = new ResiliXException("Error message");
        assertEquals("Error message", re.getMessage());
        assertNull(re.getCause());

        NullPointerException npe = new NullPointerException();
        ResiliXException reCause = new ResiliXException("Error message", npe);

        assertEquals("Error message", reCause.getMessage());
        assertSame(npe, reCause.getCause());
    }

}