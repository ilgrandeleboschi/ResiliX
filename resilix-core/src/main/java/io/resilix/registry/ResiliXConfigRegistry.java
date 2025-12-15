package io.resilix.registry;

import java.util.Map;

public interface ResiliXConfigRegistry<C> {

    C get(String name);

    void register(String name, C config);

    Map<String, C> global();

}
