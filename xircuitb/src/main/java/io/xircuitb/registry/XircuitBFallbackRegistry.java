package io.xircuitb.registry;

import io.resilix.registry.ResiliXFallbackRegistry;
import io.xircuitb.exception.XircuitBConfigurationException;
import io.xircuitb.provider.XircuitBFallbackProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.resilix.util.ResiliXUtils.fmt;

@Component
@RequiredArgsConstructor
public class XircuitBFallbackRegistry implements ResiliXFallbackRegistry<XircuitBFallbackProvider> {

    private final Map<String, XircuitBFallbackProvider> registry = new ConcurrentHashMap<>();
    private final ApplicationContext ctx;

    @Override
    public XircuitBFallbackProvider get(String name) {
        XircuitBFallbackProvider fallback = registry.get(name);
        if (fallback == null) {
            throw new XircuitBConfigurationException(fmt("Fallback %s is not registered", name));
        }
        return fallback;
    }

    @Override
    public void register(String name, Class<? extends XircuitBFallbackProvider> clazz) {
        try {
            registry.putIfAbsent(name, ctx.getBean(clazz));
        } catch (NoSuchBeanDefinitionException e) {
            throw new XircuitBConfigurationException(fmt("Fallback class %s is not a Spring bean", clazz.getSimpleName()));
        }
    }

}

