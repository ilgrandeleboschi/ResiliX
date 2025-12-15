package io.resilix.util;

import io.resilix.model.ActivePeriod;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static io.resilix.model.ActivePeriod.buildActivePeriod;
import static io.resilix.util.ResiliXUtils.daysToList;
import static io.resilix.util.ResiliXUtils.getOrDefault;
import static io.resilix.util.ResiliXUtils.unwrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    @Test
    void unwrapTest() {
        Throwable original = new RuntimeException("original");
        assertEquals(original, unwrap(original));

        CompletionException ce = new CompletionException(original);
        assertEquals(original, unwrap(ce));

        ExecutionException ee = new ExecutionException(original);
        assertEquals(original, unwrap(ee));

        CompletionException nested = new CompletionException(new ExecutionException(original));
        assertEquals(original, unwrap(nested));
    }

    @Test
    void getOrDefaultTest() {
        assertEquals(1, getOrDefault(null, 1, i -> i <= 1));
        assertEquals(1, getOrDefault(1, 1, i -> i <= 1));
        assertEquals(1, getOrDefault(1, 1, i -> i < 1));
    }

    @Test
    void daysToListTest() {
        assertEquals(List.of(DayOfWeek.values()), daysToList(DayOfWeek.values()));
        assertEquals(List.of(DayOfWeek.values()), daysToList(null));
    }

    @Test
    void buildActivePeriodTest() {
        ActivePeriod activePeriod = buildActivePeriod("09:00", "18:00", new DayOfWeek[]{DayOfWeek.MONDAY});
        assertEquals(LocalTime.of(9, 0), activePeriod.from());
        assertEquals(LocalTime.of(18, 0), activePeriod.to());
        assertEquals(List.of(DayOfWeek.MONDAY), activePeriod.activeDays());

        activePeriod = buildActivePeriod(null, null, null);
        assertNull(activePeriod.activeDays());
        assertNull(activePeriod.from());
        assertNull(activePeriod.to());
    }

}