package io.resilix.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

import static io.resilix.validator.ResiliXValidator.validateAndConvertTime;

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

    public boolean isEmpty() {
        return from() == null && to() == null && activeDays() == null;
    }

    public static ActivePeriod empty() {
        return new ActivePeriod(null, null, null);
    }

    public static ActivePeriod buildActivePeriod(String from, String to, DayOfWeek[] activeDays) {
        if ((from == null || from.isBlank()) && (to == null || to.isBlank()) && (activeDays == null || activeDays.length == 0))
            return ActivePeriod.empty();
        return new ActivePeriod(validateAndConvertTime(from), validateAndConvertTime(to), List.of(activeDays));
    }

    @NotNull
    @Override
    public String toString() {
        if (isEmpty()) return "always on";

        String days = (CollectionUtils.isEmpty(activeDays) || EnumSet.copyOf(activeDays).size() == DayOfWeek.values().length)
                ? "every day"
                : String.join(",", activeDays.stream().map(DayOfWeek::toString).toList());

        String time = (from == null || to == null) ? "anytime" : from + "-" + to;
        return time + " " + days;
    }

}
