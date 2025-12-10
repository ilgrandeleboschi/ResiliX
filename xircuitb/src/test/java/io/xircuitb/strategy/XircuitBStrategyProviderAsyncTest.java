package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBCacheModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import utils.Fixture;
import utils.MockFallbackProviderAsync;

import java.lang.reflect.Method;
import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static utils.MockBuilder.FIXED_CLOCK;
import static utils.MockBuilder.createXircuitBConfigModel;

@ExtendWith(MockitoExtension.class)
class XircuitBStrategyProviderAsyncTest {

    @Mock
    CircuitBreakerRegistry registry;
    @Mock
    XircuitBConfigFactory factory;

    Clock clock = FIXED_CLOCK;

    XircuitBStrategyProviderAsync strategy;

    @BeforeEach
    void setUp() {
        strategy = new XircuitBStrategyProviderAsync(registry, factory, clock);
        when(factory.resolveXbName(any(), any(), anyInt())).thenReturn("test");
    }

    @Test
    void decorate_wrapsSupplierAndCallsCircuitBreaker() throws Exception {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        Fixture.SimpleXb instance = new Fixture.SimpleXb();

        assertTrue(strategy.support(method.getAnnotation(XircuitB.class)));
        assertEquals(0, strategy.priority());

        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.supplyAsync(() -> {
            try {
                return method.invoke(instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        when(factory.resolveConfig(any())).thenReturn(createXircuitBConfigModel());
        when(registry.circuitBreaker(anyString(), any(CircuitBreakerConfig.class))).thenReturn(mock(CircuitBreaker.class));

        Supplier<CompletionStage<Object>> wrapped = strategy.decorate(original, method);

        assertThat(wrapped.get().toCompletableFuture().get()).isEqualTo("executed");
    }

    @Test
    void decorate_wrapsSupplier_callsFallbackOnCallNotPermitted() throws NoSuchMethodException, ExecutionException, InterruptedException {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.completedFuture("ok");
        when(factory.resolveAsyncFallback(any())).thenReturn(new MockFallbackProviderAsync());

        XircuitBStrategyProviderAsync spy = spy(strategy);
        CircuitBreaker cb = mock(CircuitBreaker.class);
        XircuitBCacheModel cache = new XircuitBCacheModel(cb, createXircuitBConfigModel());
        doReturn(cache).when(spy).computeCache(anyString(), any());

        when(cb.executeCompletionStage(any())).thenReturn(CompletableFuture.failedFuture(mock(CallNotPermittedException.class)));

        Supplier<CompletionStage<Object>> wrapped = spy.decorate(original, method);
        Object result = wrapped.get().toCompletableFuture().get();
        assertEquals("Fallback executed", result);
    }


    @Test
    void decorate_wrapsSupplier_callsException() throws NoSuchMethodException {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.completedFuture("ok");
        when(factory.resolveAsyncFallback(any())).thenReturn(new MockFallbackProviderAsync());

        XircuitBStrategyProviderAsync spy = spy(strategy);
        CircuitBreaker cb = mock(CircuitBreaker.class);
        XircuitBCacheModel cache = new XircuitBCacheModel(cb, createXircuitBConfigModel());
        doReturn(cache).when(spy).computeCache(anyString(), any());

        Exception e = new Exception("Regular exception");
        when(cb.executeCompletionStage(any())).thenReturn(CompletableFuture.failedFuture(e));

        Supplier<CompletionStage<Object>> wrapped = spy.decorate(original, method);
        Exception actual = assertThrows(Exception.class, () -> wrapped.get().toCompletableFuture().get());
        assertEquals("Regular exception", actual.getCause().getMessage());
    }

}
