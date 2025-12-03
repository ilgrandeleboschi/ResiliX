package io.xircuitb.provider;

import io.xircuitb.model.XircuitBConfigModel;

public class VoidConfigProvider implements XircuitBConfigProvider {

    @Override
    public XircuitBConfigModel apply() {
        return null;
    }
}
