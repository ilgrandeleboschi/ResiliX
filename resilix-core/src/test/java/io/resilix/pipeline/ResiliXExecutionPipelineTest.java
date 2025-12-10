package io.resilix.pipeline;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.strategy.ResiliXStrategyAsync;
import io.resilix.strategy.ResiliXStrategySync;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResiliXExecutionPipelineTest {

    @Test
    void execute_withSyncStrategy_appliesStrategy() throws Throwable {
        ResiliXStrategySync sync = mock(ResiliXStrategySync.class);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method dummyMethod = Object.class.getMethod("toString");

        when(pjp.proceed()).thenReturn("original");
        when(sync.decorate(Mockito.<CheckedSupplier<Object>>any(), eq(dummyMethod))).thenReturn(() -> "result");


        ResiliXExecutionPipeline pipeline = new ResiliXExecutionPipeline(List.of(sync));
        Object result = pipeline.execute(pjp, dummyMethod);

        assertThat(result).isEqualTo("result");
        verify(sync).decorate(Mockito.<CheckedSupplier<Object>>any(), eq(dummyMethod));
    }

    @Test
    void execute_withAsyncStrategy_appliesStrategy() throws Throwable {
        ResiliXStrategyAsync async = mock(ResiliXStrategyAsync.class);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method dummyMethod = AsyncDummy.class.getMethod("asyncMethod");

        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.completedFuture("original");
        when(pjp.proceed()).thenReturn(original.get());
        when(async.decorate(Mockito.<Supplier<CompletionStage<Object>>>any(), eq(dummyMethod)))
                .thenAnswer(invocation -> (Supplier<CompletionStage<Object>>) () -> CompletableFuture.completedFuture("asyncResult"));

        ResiliXExecutionPipeline pipeline = new ResiliXExecutionPipeline(List.of(async));
        Object result = pipeline.execute(pjp, dummyMethod);

        CompletionStage<?> stage = (CompletionStage<?>) result;
        assertThat(stage.toCompletableFuture().get()).isEqualTo("asyncResult");
        verify(async).decorate(Mockito.<Supplier<CompletionStage<Object>>>any(), eq(dummyMethod));
    }

    @Test
    void execute_withNoStrategies_returnsOriginal() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method dummyMethod = Object.class.getMethod("toString");

        when(pjp.proceed()).thenReturn("ok");

        ResiliXExecutionPipeline pipeline = new ResiliXExecutionPipeline(List.of());
        Object result = pipeline.execute(pjp, dummyMethod);

        assertThat(result).isEqualTo("ok");
    }

    static class AsyncDummy {
        public CompletionStage<String> asyncMethod() {
            return CompletableFuture.completedFuture("ok");
        }
    }
}
