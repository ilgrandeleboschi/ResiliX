package io.xircuitb.provider;

import io.xircuitb.model.XircuitBConfigModel;

@FunctionalInterface
public interface XircuitBConfigProvider {

    XircuitBConfigModel apply();

}
