package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.functions.CheckedSupplier;
import io.xircuitb.annotation.XircuitBs;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBCacheModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.Fixture;
import utils.MockFallbackProviderSync;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static utils.MockBuilder.FIXED_CLOCK;
import static utils.MockBuilder.createResiliXContext;
import static utils.MockBuilder.createXircuitBConfigModel;

@ExtendWith(MockitoExtension.class)
class XircuitBStrategyProviderSyncTest {

    @Mock
    CircuitBreakerRegistry registry;
    @Mock
    XircuitBConfigFactory factory;

    Clock clock = FIXED_CLOCK;

    XircuitBStrategyProviderSync strategy;

    @BeforeEach
    void setUp() {
        strategy = new XircuitBStrategyProviderSync(registry, factory, clock);
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("test");
    }

    @Test
    void apply_wrapsSupplierAndCallsCircuitBreaker() throws Throwable {
        Fixture.SimpleXb instance = new Fixture.SimpleXb();
        Method method = instance.getClass().getMethod("singleXb");

        Annotation ann = mock(XircuitBs.class);
        assertTrue(strategy.support(ann));
        assertEquals(0, strategy.priority());

        CheckedSupplier<Object> original = () -> method.invoke(instance);
        when(factory.resolveConfig(any())).thenReturn(createXircuitBConfigModel());
        when(registry.circuitBreaker(anyString(), any(CircuitBreakerConfig.class))).thenReturn(mock(CircuitBreaker.class));

        CheckedSupplier<Object> wrapped = strategy.decorate(original, createResiliXContext(method));

        Object result = wrapped.get();
        assertThat(result).isEqualTo("executed");
    }

    @Test
    void apply_wrapsSupplier_callsFallbackOnCallNotPermitted() throws Throwable {
        Fixture.SimpleXb instance = new Fixture.SimpleXb();
        Method method = instance.getClass().getMethod("singleXb");
        CheckedSupplier<Object> original = () -> method.invoke(instance);

        XircuitBStrategyProviderSync spy = spy(strategy);
        CircuitBreaker cb = mock(CircuitBreaker.class);
        XircuitBCacheModel cache = new XircuitBCacheModel(cb, createXircuitBConfigModel(), new MockFallbackProviderSync());
        doReturn(cache).when(spy).computeCache(anyString(), any());

        when(cb.executeCheckedSupplier(any())).thenThrow(CallNotPermittedException.class);

        CheckedSupplier<Object> wrapped = spy.decorate(original, createResiliXContext(method));
        assertEquals("Fallback executed", wrapped.get());
    }

    @Test
    void apply_failedConfiguration_returnBaseMethod() throws Throwable {
        Fixture.SimpleXb instance = new Fixture.SimpleXb();
        Method method = instance.getClass().getMethod("singleXb");
        CheckedSupplier<Object> original = () -> method.invoke(instance);

        when(factory.resolveConfig(any())).thenThrow(NullPointerException.class);

        XircuitBStrategyProviderSync spy = spy(strategy);
        CheckedSupplier<Object> wrapped = spy.decorate(original, createResiliXContext(method));
        assertEquals("executed", wrapped.get());
    }

}
