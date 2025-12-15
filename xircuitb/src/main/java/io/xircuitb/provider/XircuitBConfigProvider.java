package io.xircuitb.provider;

import io.resilix.provider.ResiliXConfigProvider;
import io.xircuitb.model.XircuitBConfigModel;

public interface XircuitBConfigProvider extends ResiliXConfigProvider<XircuitBConfigModel> {

    @Override
    XircuitBConfigModel get();

}
