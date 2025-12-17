package io.resilix.pipeline;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategyAsync;
import io.resilix.strategy.ResiliXStrategySync;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import util.Dummy;
import util.DummyAnn;
import util.DummyCacheModel;
import util.DummyConfigModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static util.ContextBuilder.getContext;

class ResiliXExecutionPipelineTest {

    ResiliXContext syncContext;
    ResiliXContext asyncContext;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        syncContext = getContext(Dummy.class.getMethod("syncMethod"));
        asyncContext = getContext(Dummy.class.getMethod("asyncMethod"));
    }

    @Test
    void execute_withSyncStrategy_appliesStrategy() throws Throwable {
        ResiliXStrategySync<DummyAnn, DummyConfigModel, DummyCacheModel> sync = mock(ResiliXStrategySync.class);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        ResiliXStrategyPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> composite = new ResiliXStrategyPipeline<>(List.of(sync));

        when(pjp.proceed()).thenReturn("original");
        when(sync.decorate(Mockito.<CheckedSupplier<Object>>any(), eq(syncContext))).thenReturn(() -> "result");

        ResiliXExecutionPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> pipeline = new ResiliXExecutionPipeline<>(composite);
        Object result = pipeline.execute(pjp, syncContext);

        assertThat(result).isEqualTo("result");
        verify(sync).decorate(Mockito.<CheckedSupplier<Object>>any(), eq(syncContext));
    }

    @Test
    void execute_withAsyncStrategy_appliesStrategy() throws Throwable {
        ResiliXStrategyAsync<DummyAnn, DummyConfigModel, DummyCacheModel> async = mock(ResiliXStrategyAsync.class);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        ResiliXStrategyPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> composite = new ResiliXStrategyPipeline<>(List.of(async));

        Supplier<CompletionStage<Object>> original = () -> CompletableFuture.completedFuture("original");
        when(pjp.proceed()).thenReturn(original.get());
        when(async.decorate(Mockito.<Supplier<CompletionStage<Object>>>any(), eq(asyncContext)))
                .thenAnswer(invocation -> (Supplier<CompletionStage<Object>>) () -> CompletableFuture.completedFuture("asyncResult"));

        ResiliXExecutionPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> pipeline = new ResiliXExecutionPipeline<>(composite);
        Object result = pipeline.execute(pjp, asyncContext);

        CompletionStage<?> stage = (CompletionStage<?>) result;
        assertThat(stage.toCompletableFuture().get()).isEqualTo("asyncResult");
        verify(async).decorate(Mockito.<Supplier<CompletionStage<Object>>>any(), eq(asyncContext));
    }

    @Test
    void execute_withNoStrategies_returnsOriginal() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        when(pjp.proceed()).thenReturn("ok");

        ResiliXExecutionPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> pipeline = new ResiliXExecutionPipeline<>(new ResiliXStrategyPipeline<>(List.of()));
        Object result = pipeline.execute(pjp, syncContext);

        assertThat(result).isEqualTo("ok");
    }

    @Test
    void execute_withSyncStrategy_throwError() throws Throwable {
        ResiliXStrategySync<DummyAnn, DummyConfigModel, DummyCacheModel> sync = mock(ResiliXStrategySync.class);
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);

        ResiliXStrategyPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> composite = new ResiliXStrategyPipeline<>(List.of(sync));

        when(pjp.proceed()).thenReturn("original");
        when(sync.decorate(Mockito.<CheckedSupplier<Object>>any(), eq(syncContext))).thenThrow(new RuntimeException("onError"));

        ResiliXExecutionPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> pipeline = new ResiliXExecutionPipeline<>(composite);

        RuntimeException e = assertThrows(RuntimeException.class, () -> pipeline.execute(pjp, syncContext));
        assertEquals("onError", e.getMessage());
        verify(sync).decorate(Mockito.<CheckedSupplier<Object>>any(), eq(syncContext));
    }

}
