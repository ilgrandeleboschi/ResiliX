package util;

import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategyAsync;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public class DummyStrategyAsync implements ResiliXStrategyAsync<DummyAnn, Object, Object> {
    @Override
    public Class<DummyAnn> support() {
        return DummyAnn.class;
    }

    @Override
    public int priority() {
        return 1;
    }

    @Override
    public Object createConfiguration(DummyAnn annotation, ResiliXContext ctx) {
        return null;
    }

    @Override
    public Object computeCache(String key, DummyAnn annotation, ResiliXContext ctx) {
        return null;
    }

    @Override
    public String resolveName(DummyAnn annotation, ResiliXContext ctx, int index) {
        return "";
    }

    @Override
    public Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> execution, ResiliXContext ctx) {
        return execution;
    }
}
