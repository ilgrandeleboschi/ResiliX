package io.xircuitb.registry;

import io.xircuitb.exception.XircuitBConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import util.MockFallbackProviderAsync;
import util.MockFallbackProviderSync;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class XircuitBFallbackRegistryTest {

    @Mock
    private ApplicationContext ctx;
    @InjectMocks
    private XircuitBFallbackRegistry registry;

    @Test
    void register_andGet_returnsFallback() {
        MockFallbackProviderSync fallback = mock(MockFallbackProviderSync.class);
        when(ctx.getBean(MockFallbackProviderSync.class)).thenReturn(fallback);
        registry.register("test", MockFallbackProviderSync.class);
        assertSame(fallback, registry.get("test"));
    }

    @Test
    void get_throwsWhenFallbackNotRegistered() {
        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> registry.get("missing")
        );

        assertTrue(ex.getMessage().contains("Fallback missing is not registered"));
    }

    @Test
    void register_throwsWhenClassIsNotSpringBean() {
        when(ctx.getBean(MockFallbackProviderSync.class))
                .thenThrow(new NoSuchBeanDefinitionException(MockFallbackProviderSync.class));

        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> registry.register("test", MockFallbackProviderSync.class)
        );

        assertTrue(ex.getMessage().contains("is not a Spring bean"));
    }

    @Test
    void register_doesNotOverrideExistingFallback() {
        MockFallbackProviderSync first = mock(MockFallbackProviderSync.class);
        MockFallbackProviderAsync second = mock(MockFallbackProviderAsync.class);

        when(ctx.getBean(MockFallbackProviderSync.class)).thenReturn(first);
        when(ctx.getBean(MockFallbackProviderAsync.class)).thenReturn(second);

        registry.register("test", MockFallbackProviderSync.class);
        registry.register("test", MockFallbackProviderAsync.class);

        assertSame(first, registry.get("test"));
    }

}