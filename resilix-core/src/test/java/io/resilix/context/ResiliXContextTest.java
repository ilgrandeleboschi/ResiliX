package io.resilix.context;

import io.resilix.model.ResiliXContext;
import org.junit.jupiter.api.Test;
import util.Dummy;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static util.ContextBuilder.defaultContext;

class ResiliXContextTest {

    @Test
    void putTest() throws NoSuchMethodException {
        Method dummyMethod = Dummy.class.getMethod("syncMethod");
        ResiliXContext ctx = defaultContext();

        ctx.put("test string", "test");
        ctx.put("test number", 1);
        ctx.putSingleton(Method.class, dummyMethod);

        assertEquals("test", ctx.get("test string", String.class));
        assertEquals(1, ctx.get("test number", Integer.class));
        assertEquals(dummyMethod, ctx.getSingleton(Method.class));
    }

}