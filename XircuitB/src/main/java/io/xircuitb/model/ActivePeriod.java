package io.xircuitb.model;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

public record ActivePeriod(LocalTime from, LocalTime to, List<DayOfWeek> activeDays) {

    public boolean isActiveNow(Clock clock) {
        EnumSet<DayOfWeek> activeDaysSet =
                (activeDays == null || activeDays.isEmpty())
                        ? EnumSet.allOf(DayOfWeek.class)
                        : EnumSet.copyOf(activeDays);

        if (!activeDaysSet.contains(LocalDate.now(clock).getDayOfWeek())) return false;

        if (from == null || to == null) return true;
        if (from.equals(to)) return false;

        LocalTime now = LocalTime.now(clock);

        return from.isBefore(to)
                ? !now.isBefore(from) && !now.isAfter(to)
                : !now.isBefore(from) || !now.isAfter(to);
    }

}
