package io.resilix.registry;

public interface ResiliXFallbackRegistry<F> {

    F get(String name);

    void register(String name, Class<? extends F> clazz);

}
