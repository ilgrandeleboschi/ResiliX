package io.xircuitb.provider.defaults;

import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.provider.XircuitBConfigProvider;

public class VoidXircuitBConfigProvider implements XircuitBConfigProvider {

    @Override
    public String name() {
        return "";
    }

    @Override
    public XircuitBConfigModel get() {
        return null;
    }
}
