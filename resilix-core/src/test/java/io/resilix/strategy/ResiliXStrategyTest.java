package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResiliXStrategyTest {

    static class DummyStrategy implements ResiliXStrategy {
        @Override
        public boolean support(Annotation annotation) {
            return false;
        }

        @Override
        public int priority() {
            return 0;
        }
    }

    @Test
    void decorateDefaultTest() throws Throwable {
        DummyStrategy strategy = new DummyStrategy();
        Method dummyMethod = Object.class.getMethod("toString");

        ResiliXContext ctx = new ResiliXContext(dummyMethod, new Object[0], dummyMethod.getAnnotations(), Map.of());

        CheckedSupplier<Object> syncSupplier = () -> "ok";
        CheckedSupplier<Object> syncResult = strategy.decorate(syncSupplier, ctx);
        assertThat(syncResult.get()).isEqualTo("ok");
        assertThat(syncResult).isSameAs(syncSupplier);

        Supplier<CompletionStage<Object>> asyncSupplier = () -> CompletableFuture.completedFuture("ok");
        Supplier<CompletionStage<Object>> asyncResult = strategy.decorate(asyncSupplier, ctx);
        assertThat(asyncResult.get().toCompletableFuture().get()).isEqualTo("ok");
        assertThat(asyncResult).isSameAs(asyncSupplier);
    }

    @Test
    void hooksTest() {
        DummyStrategy strategy = new DummyStrategy();
        assertNotNull(strategy);

        ResiliXContext ctx = new ResiliXContext();

        strategy.onEnter(ctx);
        strategy.onError(new Exception(), ctx);
        strategy.onExit(ctx);
        strategy.onSuccess("success", ctx);
    }

}
