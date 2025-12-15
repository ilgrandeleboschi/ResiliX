package io.xircuitb.factory;

import io.xircuitb.annotation.XircuitB;
import org.junit.jupiter.api.Test;
import util.Fixture;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static util.XircuitBMockBuilder.createResiliXContext;

class XircuitBNameFactoryTest {

    XircuitBNameFactory nameFactory = new XircuitBNameFactory();

    @Test
    void resolveName_nameNotEmpty() {
        XircuitB xircuitB = mock(XircuitB.class);
        when(xircuitB.name()).thenReturn("XB");
        assertEquals("XB", nameFactory.resolveName(
                xircuitB,
                null,
                0));
    }

    @Test
    void resolveName_nameEmpty() throws NoSuchMethodException {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        assertEquals("SimpleXb.singleXb#a94cefe2_1", nameFactory.resolveName(
                method.getAnnotation(XircuitB.class),
                createResiliXContext(method),
                1));
    }

}