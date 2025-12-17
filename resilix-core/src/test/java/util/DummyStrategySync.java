package util;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategySync;

public class DummyStrategySync implements ResiliXStrategySync<DummyAnn, DummyConfigModel, DummyCacheModel> {
    @Override
    public Class<DummyAnn> support() {
        return DummyAnn.class;
    }

    @Override
    public int priority() {
        return 1;
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

    @Override
    public CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, ResiliXContext ctx) {
        return execution;
    }
}