package io.xircuitb.factory;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.resilix.exception.ResiliXException;
import io.resilix.model.ResiliXContext;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.exception.XircuitBConfigurationException;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.model.XircuitBDefaultPropertiesModel;
import io.xircuitb.provider.XircuitBFallbackProvider;
import io.xircuitb.registry.XircuitBConfigRegistry;
import io.xircuitb.registry.XircuitBFallbackRegistry;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import util.Fixture;
import util.MockConfigProvider;
import util.MockFallbackProviderAsync;
import util.MockFallbackProviderSync;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutionException;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static util.XircuitBMockBuilder.FIXED_CLOCK;
import static util.XircuitBMockBuilder.createXircuitBConfigModel;
import static util.XircuitBMockBuilder.defaultResiliXContext;

@ExtendWith(MockitoExtension.class)
class XircuitBConfigFactoryTest {

    @Mock
    XircuitBDefaultPropertiesModel defaultConf;
    @Mock
    ApplicationContext appCtx;
    @Mock
    XircuitBConfigRegistry configRegistry;
    @Mock
    XircuitBFallbackRegistry fallbackRegistry;
    @InjectMocks
    XircuitBConfigFactory configFactory;

    ResiliXContext ctx = defaultResiliXContext();

    @Test
    void buildCircuitBreakerConfig_notNull() {
        CircuitBreakerConfig actual = configFactory.buildCircuitBreakerConfig(createXircuitBConfigModel(), FIXED_CLOCK);
        assertEquals(COUNT_BASED, actual.getSlidingWindowType());
        assertEquals(50, actual.getFailureRateThreshold());
        assertEquals(100, actual.getSlidingWindowSize());
        assertEquals(10, actual.getMinimumNumberOfCalls());
        assertEquals(10, actual.getPermittedNumberOfCallsInHalfOpenState());
    }

    @Test
    void resolveSyncFallback() throws NoSuchMethodException {
        Method m = Fixture.FallbackProvider.class.getDeclaredMethod("fallbackProviderSync");
        XircuitB xb = m.getAnnotation(XircuitB.class);
        when(appCtx.getBean(MockFallbackProviderSync.class)).thenReturn(new MockFallbackProviderSync());
        XircuitBFallbackProvider xircuitBFallbackProvider = configFactory.resolveFallback(xb, ctx);
        MockFallbackProviderSync mockFallbackProviderSync = (MockFallbackProviderSync) xircuitBFallbackProvider;
        assertEquals("Fallback executed", mockFallbackProviderSync.apply(mock()));
    }

    @Test
    void resolveAsyncFallback() throws ExecutionException, InterruptedException, NoSuchMethodException {
        Method m = Fixture.FallbackProvider.class.getDeclaredMethod("fallbackProviderAsync");
        XircuitB xb = m.getAnnotation(XircuitB.class);
        when(appCtx.getBean(MockFallbackProviderAsync.class)).thenReturn(new MockFallbackProviderAsync());
        XircuitBFallbackProvider xircuitBFallbackProvider = configFactory.resolveFallback(xb, ctx);
        MockFallbackProviderAsync mockFallbackProviderAsync = (MockFallbackProviderAsync) xircuitBFallbackProvider;
        assertEquals("Fallback executed", mockFallbackProviderAsync.apply(mock()).toCompletableFuture().get());
    }

    @Test
    void resolveFallback_null() throws NoSuchMethodException {
        Method m = Fixture.FallbackProvider.class.getDeclaredMethod("fallbackProviderVoid");
        XircuitB xb = m.getAnnotation(XircuitB.class);
        assertNull(configFactory.resolveFallback(xb, ctx));
    }

    @Test
    void resolveFallback_notSpringBean() throws NoSuchMethodException {
        Method m = Fixture.FallbackProvider.class.getDeclaredMethod("fallbackProviderSync");
        XircuitB xb = m.getAnnotation(XircuitB.class);
        when(appCtx.getBean(MockFallbackProviderSync.class)).thenThrow(new NoSuchBeanDefinitionException(MockFallbackProviderSync.class));
        XircuitBConfigurationException actual = assertThrows(XircuitBConfigurationException.class, () -> configFactory.resolveFallback(xb, ctx));
        assertEquals("Fallback class MockFallbackProviderSync is not a Spring bean", actual.getMessage());
    }

    @Test
    void buildCircuitBreakerConfig_null() {
        assertNull(configFactory.buildCircuitBreakerConfig(null, FIXED_CLOCK));
    }

    @Test
    void resolveConfig_defineExceptionsAndActiveDayInline() throws NoSuchMethodException, ResiliXException {
        XircuitB xb = Fixture.SimpleXb.class.getMethod("exceptionsAndActiveDaysInline").getAnnotation(XircuitB.class);
        XircuitBConfigModel actual = configFactory.resolveConfig(xb, ctx);

        assertEquals(1, actual.getActiveSchedule().periods().getFirst().activeDays().size());
        assertEquals(Exception.class, actual.getExceptionsToCatch()[0]);
    }

    @Test
    void resolveConfig_exceptionsAndActiveDayDefaultConfig() throws NoSuchMethodException, ResiliXException {
        XircuitB xb = Fixture.SimpleXb.class.getMethod("singleXb").getAnnotation(XircuitB.class);
        when(defaultConf.getSlidingWindowType()).thenReturn("COUNT_BASED");
        when(defaultConf.getSlidingWindowSize()).thenReturn(100);
        when(defaultConf.getActiveFrom()).thenReturn("09:00");
        when(defaultConf.getActiveTo()).thenReturn("19:00");
        when(defaultConf.getExceptionsToCatch()).thenReturn(new String[]{"java.lang.Exception"});
        when(defaultConf.getActiveDays()).thenReturn(new String[]{"SUNDAY", "MONDAY"});

        XircuitBConfigModel actual = configFactory.resolveConfig(xb, ctx);


        assertEquals(2, actual.getActiveSchedule().periods().getFirst().activeDays().size());
        assertEquals(Exception.class, actual.getExceptionsToCatch()[0]);
    }

    @Test
    void resolveConfig_configProvider() throws NoSuchMethodException, ResiliXException {
        XircuitB xb = Fixture.ConfigProvider.class.getMethod("configProvider").getAnnotation(XircuitB.class);
        when(appCtx.getBean(MockConfigProvider.class)).thenReturn(new MockConfigProvider());
        XircuitBConfigModel config = configFactory.resolveConfig(xb, ctx);

        assertEquals(50, config.getFailureRateThreshold());
        assertEquals(100, config.getSlidingWindowSize());
        assertEquals("COUNT_BASED", config.getSlidingWindowType());
        assertEquals(10, config.getNumCallHalfOpen());
    }

    @Test
    void resolveConfig_exception() throws NoSuchMethodException, ResiliXException {
        XircuitB xb = Fixture.SimpleXb.class.getMethod("singleXb").getAnnotation(XircuitB.class);
        when(defaultConf.getSlidingWindowType()).thenReturn("COUNT_BASED");
        when(defaultConf.getSlidingWindowSize()).thenReturn(100);
        when(defaultConf.getActiveFrom()).thenReturn("09:00");
        when(defaultConf.getActiveTo()).thenReturn("19:00");
        when(defaultConf.getExceptionsToCatch()).thenReturn(new String[]{"java.lang.test"});
        when(defaultConf.getActiveDays()).thenReturn(new String[]{"SUNDAY", "MONDAY"});

        XircuitBConfigurationException actual = assertThrows(XircuitBConfigurationException.class, () -> configFactory.resolveConfig(xb, ctx));

        assertEquals("Exception class is not a valid exception: java.lang.test", actual.getMessage());
    }

    @Test
    void resolveConfig_configTemplateNotRegistered_throw() throws NoSuchMethodException {
        XircuitB xb = Fixture.ConfigProvider.class.getMethod("configTemplate").getAnnotation(XircuitB.class);
        when(configRegistry.get(anyString())).thenReturn(null);
        XircuitBConfigurationException actual = assertThrows(XircuitBConfigurationException.class, () -> configFactory.resolveConfig(xb, ctx));
        assertEquals("XircuitB config template 'test' was referenced but not registered", actual.getMessage());
    }

    @Test
    void resolveConfig_configTemplate() throws NoSuchMethodException {
        XircuitB xb = Fixture.ConfigProvider.class.getMethod("configTemplate").getAnnotation(XircuitB.class);
        when(configRegistry.get(anyString())).thenReturn(createXircuitBConfigModel());
        XircuitBConfigModel actual = configFactory.resolveConfig(xb, ctx);
        assertEquals(100, actual.getSlidingWindowSize());
    }

    @Test
    void resolveFallback_fallbackTemplateNotRegistered_throw() throws NoSuchMethodException {
        XircuitB xb = Fixture.FallbackProvider.class.getMethod("fallbackTemplate").getAnnotation(XircuitB.class);
        when(fallbackRegistry.get(anyString())).thenReturn(null);
        XircuitBConfigurationException actual = assertThrows(XircuitBConfigurationException.class, () -> configFactory.resolveFallback(xb, ctx));
        assertEquals("XircuitB fallback template 'test' was referenced but not registered", actual.getMessage());
    }

    @Test
    void resolveFallback_fallbackTemplate() throws NoSuchMethodException {
        XircuitB xb = Fixture.FallbackProvider.class.getMethod("fallbackTemplate").getAnnotation(XircuitB.class);
        when(fallbackRegistry.get(anyString())).thenReturn(new MockFallbackProviderSync());
        XircuitBFallbackProvider actual = configFactory.resolveFallback(xb, ctx);
        assertNotNull(actual);
    }

}