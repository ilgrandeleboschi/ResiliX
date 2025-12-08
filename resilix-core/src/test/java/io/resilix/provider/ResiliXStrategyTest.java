package io.resilix.provider;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResiliXStrategyTest {

    ResiliXStrategy strategy = execution -> null;

    @Test
    void test() {
        assertEquals("", strategy.name());
    }

}