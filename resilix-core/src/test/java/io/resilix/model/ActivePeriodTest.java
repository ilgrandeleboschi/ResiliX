package io.resilix.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActivePeriodTest {

    @Test
    void activeAllDay_shouldReturnTrue() {
        ActivePeriod period = new ActivePeriod(
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                List.of()
        );

        Clock fixed = Clock.fixed(
                LocalDateTime.of(2025, 1, 15, 12, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );

        assertTrue(period.isActiveNow(fixed));
    }

    @Test
    void inactiveDay_shouldReturnFalseImmediately() {
        ActivePeriod period = new ActivePeriod(
                LocalTime.of(0, 0),
                LocalTime.of(23, 59),
                List.of(DayOfWeek.MONDAY)
        );

        Clock wednesday = Clock.fixed(
                LocalDateTime.of(2025, 1, 15, 12, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );

        assertFalse(period.isActiveNow(wednesday));
    }

    @Test
    void timeRangeOutside_shouldReturnFalse() {
        ActivePeriod period = new ActivePeriod(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0),
                List.of()
        );

        Clock fixed = Clock.fixed(
                LocalDateTime.of(2025, 1, 15, 7, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );

        assertFalse(period.isActiveNow(fixed));
    }

    @Test
    void timeRangeFromEqualsTo_shouldReturnTrue() {
        ActivePeriod period = new ActivePeriod(
                LocalTime.of(12, 0),
                LocalTime.of(12, 0),
                null
        );

        Clock fixed = Clock.fixed(
                LocalDateTime.of(2025, 1, 15, 7, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );

        assertFalse(period.isActiveNow(fixed));
    }

    @Test
    void overnightRange_shouldReturnTrueInsideNight() {
        ActivePeriod period = new ActivePeriod(
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                List.of()
        );

        Clock fixed = Clock.fixed(
                LocalDateTime.of(2025, 1, 15, 23, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );

        assertTrue(period.isActiveNow(fixed));
    }

    @Test
    void overnightRange_shouldReturnFalseOutsideNight() {
        ActivePeriod period = new ActivePeriod(
                LocalTime.of(22, 0),
                LocalTime.of(6, 0),
                List.of()
        );

        Clock fixed = Clock.fixed(
                LocalDateTime.of(2025, 1, 15, 15, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );

        assertFalse(period.isActiveNow(fixed));
    }

    @Test
    void fromAndToNull_shouldReturnTrue() {
        ActivePeriod period = new ActivePeriod(
                null,
                null,
                List.of()
        );

        Clock fixed = Clock.fixed(
                LocalDateTime.of(2025, 1, 15, 15, 0).toInstant(ZoneOffset.UTC),
                ZoneId.of("UTC")
        );

        assertTrue(period.isActiveNow(fixed));
    }

    @Test
    void isEmptyTest() {
        assertTrue(ActivePeriod.empty().isEmpty());
    }

    @Test
    void toStringTest() {
        assertEquals("always on", ActivePeriod.empty().toString());

        ActivePeriod activePeriod = new ActivePeriod(LocalTime.of(9, 0), LocalTime.of(18, 0), null);
        assertEquals("09:00-18:00 every day", activePeriod.toString());

        activePeriod = new ActivePeriod(LocalTime.of(9, 0), LocalTime.of(18, 0), List.of(DayOfWeek.values()));
        assertEquals("09:00-18:00 every day", activePeriod.toString());

        activePeriod = new ActivePeriod(LocalTime.of(9, 0), LocalTime.of(18, 0), List.of(DayOfWeek.MONDAY, DayOfWeek.SUNDAY));
        assertEquals("09:00-18:00 MONDAY,SUNDAY", activePeriod.toString());

        activePeriod = new ActivePeriod(null, LocalTime.of(18, 0), List.of(DayOfWeek.values()));
        assertEquals("anytime every day", activePeriod.toString());
    }

}
