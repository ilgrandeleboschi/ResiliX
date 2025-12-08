package io.xircuitb.provider.defaults;

import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.provider.XircuitBConfigProvider;

public class VoidConfigProvider implements XircuitBConfigProvider {

    @Override
    public XircuitBConfigModel apply() {
        return null;
    }
}
