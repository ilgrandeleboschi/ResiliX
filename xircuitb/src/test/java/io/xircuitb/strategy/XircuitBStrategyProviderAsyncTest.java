package io.xircuitb.strategy;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xircuitb.annotation.XircuitB;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static util.XircuitBMockBuilder.FIXED_CLOCK;
import static util.XircuitBMockBuilder.createResiliXContext;
import static util.XircuitBMockBuilder.createXircuitBConfigModel;
import static util.XircuitBMockBuilder.createXircuitBConfigModelWithAsyncFallback;
import static util.XircuitBMockBuilder.defaultResiliXContext;

@ExtendWith(MockitoExtension.class)
class XircuitBStrategyProviderAsyncTest {

    @Mock
    XircuitBConfigFactory configFactory;
    @Mock
    XircuitBNameFactory nameFactory;
    @Mock
    CircuitBreakerRegistry registry;
    @Mock
    XircuitBMonitor monitor;

    Clock clock = FIXED_CLOCK;

    XircuitBStrategyProviderAsync strategy;

    @BeforeEach
    void setUp() {
        strategy = new XircuitBStrategyProviderAsync(clock, configFactory, nameFactory, registry, monitor);
        when(nameFactory.resolveName(any(), any(), anyInt())).thenReturn("test");
    }

    @Test
    void decorate_wrapsSupplierAndCallsCircuitBreaker() throws Exception {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        Fixture.SimpleXb instance = new Fixture.SimpleXb();

        assertEquals(XircuitB.class, strategy.support());
        assertEquals(0, strategy.priority());

        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.supplyAsync(() -> {
            try {
                return method.invoke(instance);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        when(configFactory.resolveConfig(any(), any())).thenReturn(createXircuitBConfigModel());
        CircuitBreaker cbMock = mock(CircuitBreaker.class);
        when(cbMock.executeCompletionStage(any()))
                .thenAnswer(invocation -> {
                    Supplier<CompletionStage<Object>> supplier =
                            invocation.getArgument(0);
                    return supplier.get();
                });
        when(registry.circuitBreaker(any(), ArgumentMatchers.<Supplier<CircuitBreakerConfig>>any())).thenReturn(cbMock);

        Supplier<CompletionStage<Object>> wrapped = strategy.decorate(original, createResiliXContext(method));

        assertThat(wrapped.get().toCompletableFuture().get()).isEqualTo("executed");
    }

    @Test
    void decorate_wrapsSupplier_callsFallbackOnCallNotPermitted() throws NoSuchMethodException, ExecutionException, InterruptedException {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.completedFuture("ok");

        XircuitBStrategyProviderAsync spy = spy(strategy);
        CircuitBreaker cb = mock(CircuitBreaker.class);
        XircuitBCacheModel cache = new XircuitBCacheModel(cb, createXircuitBConfigModelWithAsyncFallback(), defaultResiliXContext());
        doReturn(cache).when(spy).computeCache(anyString(), any(), any());

        when(cb.executeCompletionStage(any())).thenReturn(CompletableFuture.failedFuture(mock(CallNotPermittedException.class)));

        Supplier<CompletionStage<Object>> wrapped = spy.decorate(original, createResiliXContext(method));
        Object result = wrapped.get().toCompletableFuture().get();
        assertEquals("Fallback executed", result);
    }


    @Test
    void decorate_wrapsSupplier_callsException() throws NoSuchMethodException {
        Method method = Fixture.SimpleXb.class.getMethod("singleXb");
        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.completedFuture("ok");

        XircuitBStrategyProviderAsync spy = spy(strategy);
        CircuitBreaker cb = mock(CircuitBreaker.class);
        XircuitBCacheModel cache = new XircuitBCacheModel(cb, createXircuitBConfigModel(), defaultResiliXContext());
        doReturn(cache).when(spy).computeCache(anyString(), any(), any());

        Exception e = new Exception("Regular exception");
        when(cb.executeCompletionStage(any())).thenReturn(CompletableFuture.failedFuture(e));

        Supplier<CompletionStage<Object>> wrapped = spy.decorate(original, createResiliXContext(method));
        Exception actual = assertThrows(Exception.class, () -> wrapped.get().toCompletableFuture().get());
        assertEquals("Regular exception", actual.getCause().getMessage());
    }

}
