package io.resilix.aspect;

import io.resilix.strategy.ResiliXStrategyAsync;
import io.resilix.strategy.ResiliXStrategySync;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ResiliXAspectTest {

    private ResiliXAspect aspect;

    @BeforeEach
    void setUp() {
        ResiliXStrategySync syncStrategy = mock(ResiliXStrategySync.class);
        ResiliXStrategyAsync asyncStrategy = mock(ResiliXStrategyAsync.class);

        when(syncStrategy.priority()).thenReturn(1);
        when(asyncStrategy.priority()).thenReturn(1);

        when(syncStrategy.support(any())).thenReturn(true);
        when(asyncStrategy.support(any())).thenReturn(true);

        aspect = new ResiliXAspect(List.of(syncStrategy, asyncStrategy));
    }

    @Test
    void applyResiliX_executesSyncMethod() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Method method = Dummy.class.getMethod("syncMethod");
        MethodSignature signature = mock(MethodSignature.class);

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

        when(pjp.getSignature()).thenReturn(signature);
        when(signature.getMethod()).thenReturn(method);
        when(pjp.proceed()).thenThrow(new RuntimeException("error"));

        Object result = aspect.applyResiliX(pjp);

        assertThat(result).isInstanceOf(CompletableFuture.class);
        CompletableFuture<?> future = (CompletableFuture<?>) result;
        assertThrows(RuntimeException.class, future::join);
    }

    static class Dummy {
        public String syncMethod() {
            return "ok";
        }

        public CompletableFuture<String> asyncMethod() {
            return CompletableFuture.completedFuture("ok");
        }
    }
}
