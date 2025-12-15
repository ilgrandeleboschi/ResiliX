package io.resilix.pipeline;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Dummy;
import util.DummyAnn;
import util.DummyStrategyAsync;
import util.DummyStrategySync;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResiliXAspectTest {

    private ResiliXAspect<DummyAnn, Object, Object> aspect;

    @BeforeEach
    void setUp() {
        aspect = new ResiliXAspect<>(List.of(new DummyStrategySync(), new DummyStrategyAsync()));
    }

    @Test
    void applyResiliX_executesSyncMethod() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method method = Dummy.class.getMethod("syncMethod");
        MethodSignature signature = mock(MethodSignature.class);

        when(pjp.getTarget()).thenReturn(new Dummy());
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.proceed()).thenReturn("ok");

        Object result = aspect.applyResiliX(pjp);

        assertThat(result).isEqualTo("ok");
        verify(pjp).proceed();
    }

    @Test
    void applyResiliX_executesAsyncMethod() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method method = Dummy.class.getMethod("asyncMethod");
        MethodSignature signature = mock(MethodSignature.class);

        when(pjp.getTarget()).thenReturn(new Dummy());
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.proceed()).thenReturn(CompletableFuture.completedFuture("asyncOk"));

        Object result = aspect.applyResiliX(pjp);

        assertThat(result).isInstanceOf(CompletableFuture.class);
        assertThat(((CompletableFuture<?>) result).get()).isEqualTo("asyncOk");
        verify(pjp).proceed();
    }

    @Test
    void applyResiliX_asyncMethodThrowsCompletesExceptionally() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method method = Dummy.class.getMethod("asyncMethod");
        MethodSignature signature = mock(MethodSignature.class);

        when(pjp.getTarget()).thenReturn(new Dummy());
        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.proceed()).thenThrow(new RuntimeException("error"));

        Object result = aspect.applyResiliX(pjp);

        assertThat(result).isInstanceOf(CompletableFuture.class);
        CompletableFuture<?> future = (CompletableFuture<?>) result;
        assertThrows(RuntimeException.class, future::join);
    }

}
