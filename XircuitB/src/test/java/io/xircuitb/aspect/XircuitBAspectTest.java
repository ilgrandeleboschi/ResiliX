package io.xircuitb.aspect;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xircuitb.exceptions.XircuitBConfigurationException;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBConfigModel;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.Fixture;
import utils.MockFallbackProvider;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static utils.MockBuilder.FIXED_CLOCK;
import static utils.MockBuilder.createXircuitBConfigModel;

@ExtendWith(MockitoExtension.class)
class XircuitBAspectTest {

    @Mock
    CircuitBreakerRegistry registry;
    @Mock
    XircuitBConfigFactory factory;
    @Mock
    Clock clock;
    @Mock
    ProceedingJoinPoint pjp;
    @Mock
    MethodSignature signature;
    @Mock
    CircuitBreaker cb;

    @InjectMocks
    XircuitBAspect aspect;

    @Test
    void wrapsExecutionWithCircuitBreaker_whenActive() throws Throwable {
        mockPjpReturning("singleXb");
        mockClock();
        mockFactoryConfig();
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        mockRegistryCircuitBreaker();

        Object result = aspect.wrapWithCircuitBreaker(pjp);

        assertEquals("executed", result);
        verify(cb, times(1)).executeCheckedSupplier(any());
    }


    @Test
    void wrapsExecutionWithCircuitBreaker_callNotPermitted_fallback() throws Throwable {
        CallNotPermittedException ex = mock(CallNotPermittedException.class);
        Method method = Fixture.SimpleXb.class.getMethod("fallbackProvider");

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        mockClock();
        mockFactoryConfig();
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        when(factory.resolveFallback(any())).thenReturn(new MockFallbackProvider());
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenReturn(cb);
        when(cb.executeCheckedSupplier(any())).thenThrow(ex);

        Object result = aspect.wrapWithCircuitBreaker(pjp);

        assertEquals("Fallback executed", result);
        verify(cb, times(1)).executeCheckedSupplier(any());
    }

    @Test
    void bypassesCircuitBreaker_whenInactive() throws Throwable {
        mockPjpReturning("singleXb");
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        mockFactoryConfig();

        Object result = aspect.wrapWithCircuitBreaker(pjp);

        assertEquals("executed", result);
        verify(cb, never()).executeCheckedSupplier(any());
        verify(pjp, times(1)).proceed();
    }

    @Test
    void bypassesCircuitBreaker_configError() throws Throwable {
        mockPjpReturning("singleXb");
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        when(factory.resolveConfig(any())).thenThrow(new XircuitBConfigurationException("test"));

        Object result = aspect.wrapWithCircuitBreaker(pjp);

        assertEquals("executed", result);
        verify(cb, never()).executeCheckedSupplier(any());
        verify(pjp, times(1)).proceed();
    }

    @Test
    void bypassesCircuitBreaker_registryError() throws Throwable {
        mockPjpReturning("singleXb");
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        mockFactoryConfig();
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenThrow(new NullPointerException());

        Object result = aspect.wrapWithCircuitBreaker(pjp);

        assertEquals("executed", result);
        verify(cb, never()).executeCheckedSupplier(any());
        verify(pjp, times(1)).proceed();
    }

    @Test
    void appliesMultipleCircuitBreakers_whenAnnotatedWithSeveral() throws Throwable {
        mockPjpReturning("multipleXb");
        mockClock();
        mockFactoryConfig();
        when(factory.resolveXbName(any(), any(), anyInt()))
                .thenReturn("XB1")
                .thenReturn("XB2");
        mockRegistryCircuitBreaker();

        Object result = aspect.wrapWithCircuitBreaker(pjp);

        assertEquals("executed", result);
        verify(cb, times(2)).executeCheckedSupplier(any());
    }

    private void mockPjpReturning(String methodName) throws Throwable {
        Fixture.SimpleXb target = new Fixture.SimpleXb();
        Method method = Fixture.SimpleXb.class.getMethod(methodName);

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);

        when(pjp.proceed()).thenAnswer(invocation -> method.invoke(target));
    }

    private void mockClock() {
        when(clock.getZone()).thenReturn(FIXED_CLOCK.getZone());
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());
    }

    private void mockFactoryConfig() {
        XircuitBConfigModel config = createXircuitBConfigModel();
        when(factory.resolveConfig(any())).thenReturn(config);
    }

    private void mockRegistryCircuitBreaker() throws Throwable {
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenReturn(cb);
        when(cb.executeCheckedSupplier(any())).thenAnswer(inv -> {
            var supplier = (io.github.resilience4j.core.functions.CheckedSupplier<?>) inv.getArgument(0);
            return supplier.get();
        });
    }

}
