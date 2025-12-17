package util;

import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategy;

public class DummyStrategy implements ResiliXStrategy<DummyAnn, DummyConfigModel, DummyCacheModel> {
    @Override
    public Class<DummyAnn> support() {
        return null;
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public DummyConfigModel createConfiguration(DummyAnn annotation, ResiliXContext ctx) {
        return null;
    }

    @Override
    public DummyCacheModel computeCache(String key, DummyAnn annotation, ResiliXContext ctx) {
        return null;
    }

    @Override
    public String resolveName(DummyAnn annotation, ResiliXContext ctx, int index) {
        return "";
    }

}
