package io.xircuitb.registry;

import io.resilix.registry.ResiliXConfigRegistry;
import io.xircuitb.model.XircuitBConfigModel;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class XircuitBConfigRegistry implements ResiliXConfigRegistry<XircuitBConfigModel> {

    private final ConcurrentHashMap<String, XircuitBConfigModel> registry = new ConcurrentHashMap<>();

    @Override
    public XircuitBConfigModel get(String name) {
        return registry.get(name);
    }

    @Override
    public void register(String name, XircuitBConfigModel config) {
        registry.putIfAbsent(name, config);
    }

    @Override
    public ConcurrentMap<String, XircuitBConfigModel> global() {
        return registry;
    }

}
