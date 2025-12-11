package io.xircuitb.factory;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.model.XircuitBDefaultPropertiesModel;
import io.xircuitb.provider.XircuitBFallbackProvider;
import io.xircuitb.provider.defaults.VoidFallbackProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import utils.Fixture;
import utils.MockConfigProvider;
import utils.MockFallbackProviderAsync;
import utils.MockFallbackProviderSync;

import java.util.concurrent.ExecutionException;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static utils.MockBuilder.createXircuitBConfigModel;

@ExtendWith(MockitoExtension.class)
class XircuitBConfigFactoryTest {

    @Mock
    XircuitBDefaultPropertiesModel defaultConf;
    @Mock
    ApplicationContext ctx;
    @InjectMocks
    XircuitBConfigFactory factory;

    @Test
    void buildCircuitBreakerConfig_notNull() {
        CircuitBreakerConfig actual = factory.buildCircuitBreakerConfig(createXircuitBConfigModel());
        assertEquals(COUNT_BASED, actual.getSlidingWindowType());
        assertEquals(50, actual.getFailureRateThreshold());
        assertEquals(100, actual.getSlidingWindowSize());
        assertEquals(10, actual.getMinimumNumberOfCalls());
        assertEquals(10, actual.getPermittedNumberOfCallsInHalfOpenState());
    }

    @Test
    void resolveSyncFallback() {
        when(ctx.getBean(MockFallbackProviderSync.class)).thenReturn(new MockFallbackProviderSync());
        XircuitBFallbackProvider xircuitBFallbackProvider = factory.resolveFallback(MockFallbackProviderSync.class);
        MockFallbackProviderSync mockFallbackProviderSync = (MockFallbackProviderSync) xircuitBFallbackProvider;
        assertEquals("Fallback executed", mockFallbackProviderSync.apply(mock()));
    }

    @Test
    void resolveAsyncFallback() throws ExecutionException, InterruptedException {
        when(ctx.getBean(MockFallbackProviderAsync.class)).thenReturn(new MockFallbackProviderAsync());
        XircuitBFallbackProvider xircuitBFallbackProvider = factory.resolveFallback(MockFallbackProviderAsync.class);
        MockFallbackProviderAsync mockFallbackProviderAsync = (MockFallbackProviderAsync) xircuitBFallbackProvider;
        assertEquals("Fallback executed", mockFallbackProviderAsync.apply(mock()).toCompletableFuture().get());
    }

    @Test
    void resolveFallback_null() {
        assertNull(factory.resolveFallback(VoidFallbackProvider.class));
    }

    @Test
    void resolveFallback_notSpringBean() {
        when(ctx.getBean(MockFallbackProviderSync.class)).thenThrow(new NoSuchBeanDefinitionException(MockFallbackProviderSync.class));
        assertNull(factory.resolveFallback(MockFallbackProviderSync.class));
    }

    @Test
    void buildCircuitBreakerConfig_null() {
        assertNull(factory.buildCircuitBreakerConfig(null));
    }

    @Test
    void resolveXbName_nameNotEmpty() {
        XircuitB xircuitB = mock(XircuitB.class);
        when(xircuitB.name()).thenReturn("XB");
        assertEquals("XB", factory.resolveXbName(
                null,
                xircuitB,
                0));
    }

    @Test
    void resolveXbName_nameEmpty() throws NoSuchMethodException {
        assertEquals("SimpleXb.singleXb#f279a7c9_1", factory.resolveXbName(
                Fixture.SimpleXb.class.getMethod("singleXb"),
                Fixture.SimpleXb.class.getMethod("singleXb").getAnnotation(XircuitB.class),
                1));
    }

    @Test
    void resolveConfig_defineExceptionsAndActiveDayInline() throws NoSuchMethodException {
        XircuitB xb = Fixture.SimpleXb.class.getMethod("exceptionsAndActiveDaysInline").getAnnotation(XircuitB.class);
        XircuitBConfigModel actual = factory.resolveConfig(xb);

        assertEquals(1, actual.getActiveDays().length);
        assertEquals(Exception.class, actual.getExceptionsToCatch()[0]);
    }

    @Test
    void resolveConfig_exceptionsAndActiveDayDefaultConfig() throws NoSuchMethodException {
        XircuitB xb = Fixture.SimpleXb.class.getMethod("singleXb").getAnnotation(XircuitB.class);
        when(defaultConf.getSlidingWindowType()).thenReturn("COUNT_BASED");
        when(defaultConf.getSlidingWindowSize()).thenReturn(100);
        when(defaultConf.getActiveFrom()).thenReturn("09:00");
        when(defaultConf.getActiveTo()).thenReturn("19:00");
        when(defaultConf.getExceptionsToCatch()).thenReturn(new String[]{"java.lang.Exception"});
        when(defaultConf.getActiveDays()).thenReturn(new String[]{"SUNDAY", "MONDAY"});

        XircuitBConfigModel actual = factory.resolveConfig(xb);

        assertEquals(2, actual.getActiveDays().length);
        assertEquals(Exception.class, actual.getExceptionsToCatch()[0]);
    }

    @Test
    void resolveConfig_configConfigProvider() throws NoSuchMethodException {
        XircuitB xb = Fixture.ConfigProvider.class.getMethod("configProvider").getAnnotation(XircuitB.class);
        when(ctx.getBean(MockConfigProvider.class)).thenReturn(new MockConfigProvider());
        XircuitBConfigModel config = factory.resolveConfig(xb);

        assertEquals(50, config.getFailureRateThreshold());
        assertEquals(100, config.getSlidingWindowSize());
        assertEquals("COUNT_BASED", config.getSlidingWindowType());
        assertEquals(10, config.getNumCallHalfOpen());
    }

}