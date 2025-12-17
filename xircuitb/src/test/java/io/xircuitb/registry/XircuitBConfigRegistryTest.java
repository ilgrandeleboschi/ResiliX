package io.xircuitb.registry;

import io.xircuitb.model.XircuitBConfigModel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static util.XircuitBMockBuilder.createXircuitBConfigModel;

class XircuitBConfigRegistryTest {

    @Test
    void register_doesNotOverrideExistingConfig() {
        XircuitBConfigRegistry registry = new XircuitBConfigRegistry();

        XircuitBConfigModel first = createXircuitBConfigModel();
        XircuitBConfigModel second = createXircuitBConfigModel();

        registry.register("test", first);
        registry.register("test", second);

        assertSame(first, registry.get("test"));
        assertNotSame(second, registry.get("test"));
    }

    @Test
    void get_returnsNullWhenNotPresent() {
        XircuitBConfigRegistry registry = new XircuitBConfigRegistry();
        assertNull(registry.get("missing"));
    }

    @Test
    void global_exposesRegisteredConfigs() {
        XircuitBConfigRegistry registry = new XircuitBConfigRegistry();
        registry.register("test", createXircuitBConfigModel());
        assertTrue(registry.global().containsKey("test"));
    }

}