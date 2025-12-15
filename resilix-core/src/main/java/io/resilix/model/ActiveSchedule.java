package io.resilix.model;

import org.jetbrains.annotations.NotNull;

import java.time.Clock;
import java.util.List;

public record ActiveSchedule(List<ActivePeriod> periods) {

    public boolean isActiveNow(Clock clock) {
        return periods.isEmpty() || periods.stream().anyMatch(p -> p.isActiveNow(clock));
    }

    public static ActiveSchedule alwaysOn() {
        return new ActiveSchedule(List.of());
    }

    public static ActiveSchedule of(ActivePeriod period) {
        return period == null ? alwaysOn() : new ActiveSchedule(List.of(period));
    }

    public boolean isEmpty() {
        return periods == null || periods.isEmpty() || periods.stream().allMatch(ActivePeriod::isEmpty);
    }

    @NotNull
    @Override
    public String toString() {
        if (periods.isEmpty()) return "always on";
        return periods.stream()
                .map(ActivePeriod::toString)
                .reduce((a, b) -> a + "; " + b)
                .orElse("empty");
    }

}
