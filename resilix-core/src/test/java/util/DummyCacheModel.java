package util;

import io.resilix.model.ResiliXCacheModel;

public class DummyCacheModel implements ResiliXCacheModel<DummyConfigModel> {
    @Override
    public DummyConfigModel config() {
        return DummyConfigModel.builder().build();
    }
}
