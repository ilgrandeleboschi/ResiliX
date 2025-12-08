package io.resilix.aspect;

import io.resilix.provider.ResiliXStrategy;
import io.resilix.provider.ResiliXStrategyProvider;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ResiliXAspectTest {

    private ResiliXAspect aspect;
    private ResiliXStrategyProvider provider;

    @BeforeEach
    void setUp() {
        provider = mock(ResiliXStrategyProvider.class);
        aspect = new ResiliXAspect(List.of(provider));
    }

    @Test
    void testApplyResiliX_executesPipeline() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method method = TestClass.class.getMethod("sampleMethod");
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.proceed()).thenReturn("executed");

        Annotation annotation = method.getAnnotations()[0];
        when(provider.support(annotation)).thenReturn(true);
        when(provider.priority()).thenReturn(1);

        ResiliXStrategy strategy = execution -> execution;
        when(provider.strategy(annotation, method)).thenReturn(strategy);

        Object result = aspect.applyResiliX(pjp);

        assertEquals("executed", result);
        verify(pjp, times(1)).proceed();
        verify(provider, atLeastOnce()).support(annotation);
        verify(provider, atLeastOnce()).strategy(annotation, method);
    }

    static class TestClass {
        @TestAnnotation
        public String sampleMethod() {
            return "executed";
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface TestAnnotation {
    }
}
