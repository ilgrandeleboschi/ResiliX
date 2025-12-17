package io.resilix.provider;

import io.resilix.model.ResiliXConfigModel;

/**
 * Internal base interface for config providers.
 * Do not implement directly. Implement only the module version of it.
 */
public interface ResiliXConfigProvider<C extends ResiliXConfigModel> {

    String name();

    C get();

}
