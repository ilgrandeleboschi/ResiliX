package io.resilix.pipeline;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXCacheModel;
import io.resilix.model.ResiliXConfigModel;
import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategy;
import io.resilix.strategy.ResiliXStrategyAsync;
import io.resilix.strategy.ResiliXStrategySync;
import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

@RequiredArgsConstructor
class ResiliXStrategyPipeline<A extends Annotation, C extends ResiliXConfigModel, K extends ResiliXCacheModel<C>> implements ResiliXStrategySync<A, C, K>, ResiliXStrategyAsync<A, C, K> {

    private final List<ResiliXStrategy<A, C, K>> strategies;

    @Override
    public CheckedSupplier<Object> decorate(CheckedSupplier<Object> execution, ResiliXContext ctx) {
        CheckedSupplier<Object> chain = execution;
        for (int i = strategies.size() - 1; i >= 0; i--) {
            chain = strategies.get(i).decorate(chain, ctx);
        }
        return chain;
    }

    @Override
    public Supplier<CompletionStage<Object>> decorate(Supplier<CompletionStage<Object>> execution, ResiliXContext ctx) {
        Supplier<CompletionStage<Object>> chain = execution;
        for (int i = strategies.size() - 1; i >= 0; i--) {
            chain = strategies.get(i).decorate(chain, ctx);
        }
        return chain;
    }

    @Override
    public void onEnter(ResiliXContext ctx) {
        strategies.forEach(s -> s.onEnter(ctx));
    }

    @Override
    public void onSuccess(Object result, ResiliXContext ctx) {
        strategies.forEach(s -> s.onSuccess(result, ctx));
    }

    @Override
    public void onError(Throwable error, ResiliXContext ctx) {
        strategies.forEach(s -> s.onError(error, ctx));
    }

    @Override
    public void onExit(ResiliXContext ctx) {
        strategies.forEach(s -> s.onExit(ctx));
    }

    @Override
    public Class<A> support() {
        throw new UnsupportedOperationException("Composite strategy does not allow method support");
    }

    @Override
    public int priority() {
        throw new UnsupportedOperationException("Composite strategy does not allow method priority");
    }

    @Override
    public C createConfiguration(A annotation, ResiliXContext ctx) {
        throw new UnsupportedOperationException("Composite strategy does not allow method createConfiguration");
    }

    @Override
    public K computeCache(String key, A annotation, ResiliXContext ctx) {
        throw new UnsupportedOperationException("Composite strategy does not allow method computeCache");
    }

    @Override
    public String resolveName(A annotation, ResiliXContext ctx, int index) {
        throw new UnsupportedOperationException("Composite strategy does not allow method resolveName");
    }

}
