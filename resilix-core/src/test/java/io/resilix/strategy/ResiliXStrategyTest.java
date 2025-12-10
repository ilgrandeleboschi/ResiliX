package io.resilix.strategy;

import io.github.resilience4j.core.functions.CheckedSupplier;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

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

        CheckedSupplier<Object> syncSupplier = () -> "ok";
        CheckedSupplier<Object> syncResult = strategy.decorate(syncSupplier, dummyMethod);
        assertThat(syncResult.get()).isEqualTo("ok");
        assertThat(syncResult).isSameAs(syncSupplier);

        Supplier<CompletionStage<Object>> asyncSupplier = () -> CompletableFuture.completedFuture("ok");
        Supplier<CompletionStage<Object>> asyncResult = strategy.decorate(asyncSupplier, dummyMethod);
        assertThat(asyncResult.get().toCompletableFuture().get()).isEqualTo("ok");
        assertThat(asyncResult).isSameAs(asyncSupplier);
    }
}
