package io.resilix.provider;

/**
 * Internal base interface for config providers.
 * Do not implement directly. Implement only the module version of it.
 */
public interface ResiliXConfigProvider<T> {

    String name();

    T get();

}
