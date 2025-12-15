package io.resilix.model;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActiveScheduleTest {

    Clock fixed = Clock.fixed(LocalDateTime.of(2025, 1, 15, 12, 0).toInstant(ZoneOffset.UTC), ZoneId.of("UTC"));

    @Test
    void scheduleTest() {
        assertTrue(ActiveSchedule.alwaysOn().isEmpty());
        assertTrue(ActiveSchedule.alwaysOn().isActiveNow(fixed));
        assertTrue(ActiveSchedule.of(ActivePeriod.empty()).isActiveNow(fixed));
        assertEquals(ActiveSchedule.alwaysOn(), ActiveSchedule.of(null));
    }

    @Test
    void toStringTest() {
        assertEquals("always on", ActiveSchedule.alwaysOn().toString());
        assertEquals("always on", ActiveSchedule.of(ActivePeriod.empty()).toString());
        assertEquals("09:00-18:00 every day", ActiveSchedule.of(new ActivePeriod(LocalTime.of(9, 0), LocalTime.of(18, 0), List.of())).toString());
    }

}