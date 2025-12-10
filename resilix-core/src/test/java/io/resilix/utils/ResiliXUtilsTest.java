package io.resilix.utils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResiliXUtilsTest {

    @Test
    void wrapAsync_wrapsNonStageResultIntoCompletedFuture() throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.proceed()).thenReturn("ok");

        Supplier<CompletionStage<Object>> wrapped = ResiliXUtils.wrapAsync(pjp);
        CompletionStage<Object> result = wrapped.get();

        assertThat(result.toCompletableFuture().get()).isEqualTo("ok");
    }


}