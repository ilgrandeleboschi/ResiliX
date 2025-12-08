package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.provider.ResiliXStrategy;
import io.xircuitb.exceptions.XircuitBConfigurationException;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBConfigModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
class XircuitBStrategyProviderTest {

    @Mock
    CircuitBreakerRegistry registry;
    @Mock
    XircuitBConfigFactory factory;
    @Mock
    Clock clock;
    @Mock
    CircuitBreaker cb;

    XircuitBStrategyProvider provider;

    Fixture.SimpleXb simpleXb = new Fixture.SimpleXb();

    @BeforeEach
    void setup() {
        provider = new XircuitBStrategyProvider(registry, factory, clock, 1);
    }

    @Test
    void wrapsExecutionWithCircuitBreaker_whenActive() throws Throwable {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        mockClock();
        mockFactoryConfig();
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        mockRegistryCircuitBreaker();

        ResiliXStrategy strategy = provider.strategy(null, method);
        Object result = strategy.apply(() -> simpleXb.singleXb()).get();

        assertEquals("executed", result);
        verify(cb, times(1)).executeCheckedSupplier(any());
    }


    @Test
    void wrapsExecutionWithCircuitBreaker_callNotPermitted_fallback() throws Throwable {
        CallNotPermittedException ex = mock(CallNotPermittedException.class);
        Method method = Fixture.SimpleXb.class.getMethod("fallbackProvider");

        mockClock();
        mockFactoryConfig();
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        when(factory.resolveFallback(any())).thenReturn(new MockFallbackProvider());
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenReturn(cb);
        when(cb.executeCheckedSupplier(any())).thenThrow(ex);

        ResiliXStrategy strategy = provider.strategy(null, method);
        Object result = strategy.apply(() -> simpleXb.singleXb()).get();

        assertEquals("Fallback executed", result);
        verify(cb, times(1)).executeCheckedSupplier(any());
    }

    @Test
    void bypassesCircuitBreaker_whenInactive() throws Throwable {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        mockFactoryConfig();

        ResiliXStrategy strategy = provider.strategy(null, method);
        Object result = strategy.apply(() -> simpleXb.singleXb()).get();

        assertEquals("executed", result);
        verify(cb, never()).executeCheckedSupplier(any());
    }

    @Test
    void bypassesCircuitBreaker_configError() throws Throwable {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        when(factory.resolveConfig(any())).thenThrow(new XircuitBConfigurationException("test"));

        ResiliXStrategy strategy = provider.strategy(null, method);
        Object result = strategy.apply(() -> simpleXb.singleXb()).get();

        assertEquals("executed", result);
        verify(cb, never()).executeCheckedSupplier(any());
    }

    @Test
    void bypassesCircuitBreaker_registryError() throws Throwable {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("XB");
        mockFactoryConfig();
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenThrow(new NullPointerException());

        ResiliXStrategy strategy = provider.strategy(null, method);
        Object result = strategy.apply(() -> simpleXb.singleXb()).get();

        assertEquals("executed", result);
        verify(cb, never()).executeCheckedSupplier(any());
    }

    @Test
    void appliesMultipleCircuitBreakers_whenAnnotatedWithSeveral() throws Throwable {
        Method method = Fixture.SimpleXb.class.getMethod("multipleXb");
        mockClock();
        mockFactoryConfig();
        when(factory.resolveXbName(any(), any(), anyInt()))
                .thenReturn("XB1")
                .thenReturn("XB2");
        mockRegistryCircuitBreaker();

        ResiliXStrategy strategy = provider.strategy(null, method);
        Object result = strategy.apply(() -> simpleXb.singleXb()).get();

        assertEquals("executed", result);
        verify(cb, times(2)).executeCheckedSupplier(any());
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
            var supplier = (CheckedSupplier<?>) inv.getArgument(0);
            return supplier.get();
        });
    }

}
