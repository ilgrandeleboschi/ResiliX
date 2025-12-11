package io.resilix.pointcut;

import io.resilix.utils.ResiliXPointcut;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResiliXPointcutTest {

    @Test
    void pointCutTest() {
        ResiliXPointcut resiliXPointcut = new ResiliXPointcut();

        assertNotNull(resiliXPointcut);

        resiliXPointcut.xircuitB();
        resiliXPointcut.xircuitBs();
        resiliXPointcut.allResiliX();
    }

}