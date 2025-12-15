package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.exception.XircuitBConfigurationException;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.factory.XircuitBNameFactory;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.monitor.XircuitBMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import util.Fixture;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static util.XircuitBMockBuilder.FIXED_CLOCK;
import static util.XircuitBMockBuilder.createResiliXContext;
import static util.XircuitBMockBuilder.createXircuitBConfigModel;
import static util.XircuitBMockBuilder.defaultResiliXContext;

@ExtendWith(MockitoExtension.class)
class XircuitBStrategyProviderSyncTest {

    @Mock
    XircuitBConfigFactory configFactory;
    @Mock
    XircuitBNameFactory nameFactory;
    @Mock
    CircuitBreakerRegistry registry;
    @Mock
    XircuitBMonitor monitor;

    Clock clock = FIXED_CLOCK;

    XircuitBStrategyProviderSync strategy;

    @BeforeEach
    void setUp() {
        strategy = new XircuitBStrategyProviderSync(clock, configFactory, nameFactory, registry, monitor);
        when(nameFactory.resolveName(any(), any(), anyInt())).thenReturn("test");
    }

    @Test
    void apply_wrapsSupplierAndCallsCircuitBreaker() throws Throwable {
        Fixture.SimpleXb instance = new Fixture.SimpleXb();
        Method method = instance.getClass().getMethod("singleXb");

        assertEquals(XircuitB.class, strategy.support());
        assertEquals(0, strategy.priority());

        CheckedSupplier<Object> original = () -> method.invoke(instance);
        when(configFactory.resolveConfig(any(), any())).thenReturn(createXircuitBConfigModel());
        CircuitBreaker cbMock = mock(CircuitBreaker.class);
        when(cbMock.executeCheckedSupplier(any())).thenAnswer(invocation -> {
            CheckedSupplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenReturn(cbMock);

        CheckedSupplier<Object> wrapped = strategy.decorate(original, createResiliXContext(method));

        Object result = wrapped.get();
        assertThat(result).isEqualTo("executed");
    }

    @Test
    void apply_circuitBreakerCreationError_throw() throws Throwable {
        Fixture.SimpleXb instance = new Fixture.SimpleXb();
        Method method = instance.getClass().getMethod("singleXb");

        assertEquals(XircuitB.class, strategy.support());
        assertEquals(0, strategy.priority());

        CheckedSupplier<Object> original = () -> method.invoke(instance);
        when(configFactory.resolveConfig(any(), any())).thenReturn(createXircuitBConfigModel());
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenThrow(new NullPointerException("Error"));

        ResiliXContext ctx = createResiliXContext(method);
        XircuitBConfigurationException e = assertThrows(XircuitBConfigurationException.class, () -> strategy.decorate(original, ctx));
        assertEquals("Error", e.getMessage());
    }

    @Test
    void apply_wrapsSupplier_callsFallbackOnCallNotPermitted() throws Throwable {
        Fixture.SimpleXb instance = new Fixture.SimpleXb();
        Method method = instance.getClass().getMethod("singleXb");
        CheckedSupplier<Object> original = () -> method.invoke(instance);

        XircuitBStrategyProviderSync spy = spy(strategy);
        CircuitBreaker cb = mock(CircuitBreaker.class);
        XircuitBCacheModel cache = new XircuitBCacheModel(cb, createXircuitBConfigModel(), defaultResiliXContext());
        doReturn(cache).when(spy).computeCache(anyString(), any(), any());

        when(cb.executeCheckedSupplier(any())).thenThrow(CallNotPermittedException.class);

        CheckedSupplier<Object> wrapped = spy.decorate(original, createResiliXContext(method));
        assertEquals("Fallback executed", wrapped.get());
    }

    @Test
    void apply_failedConfiguration_exception() throws Throwable {
        Fixture.SimpleXb instance = new Fixture.SimpleXb();
        Method method = instance.getClass().getMethod("singleXb");
        CheckedSupplier<Object> original = () -> method.invoke(instance);

        when(configFactory.resolveConfig(any(), any())).thenThrow(NullPointerException.class);

        XircuitBStrategyProviderSync spy = spy(strategy);
        ResiliXContext ctx = createResiliXContext(method);
        XircuitBConfigurationException e = assertThrows(XircuitBConfigurationException.class, () -> spy.decorate(original, ctx));
        assertEquals("Failed to resolve configuration for singleXb", e.getMessage());
    }

}
