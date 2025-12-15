package io.resilix.validator;

import io.resilix.exception.ResiliXException;
import lombok.experimental.UtilityClass;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static io.resilix.util.ResiliXUtils.fmt;

@UtilityClass
public class ResiliXValidator {

    public static DayOfWeek[] validateAndConvertDays(String[] days) throws ResiliXException {
        if (days == null) return DayOfWeek.values();
        DayOfWeek[] result = new DayOfWeek[days.length];
        for (int i = 0; i < days.length; i++) {
            try {
                result[i] = DayOfWeek.valueOf(days[i]);
            } catch (IllegalArgumentException e) {
                throw new ResiliXException(fmt("Invalid day of week: %s", days[i]));
            }
        }
        return result;
    }

    public static Class<? extends Throwable>[] validateAndConvertExceptions(String[] names) throws ResiliXException {
        if (names == null || names.length == 0) return new Class[]{Exception.class};
        Class<? extends Throwable>[] result = new Class[names.length];
        for (int i = 0; i < names.length; i++) {
            try {
                result[i] = Class.forName(names[i]).asSubclass(Throwable.class);
            } catch (ClassNotFoundException | ClassCastException e) {
                throw new ResiliXException(fmt("Exception class is not a valid exception: %s", names[i]));
            }
        }
        return result;
    }

    public static LocalTime validateAndConvertTime(String time) throws ResiliXException {
        if (time == null || time.isBlank()) return null;
        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("H:mm"),
                DateTimeFormatter.ofPattern("HH:mm"),
                DateTimeFormatter.ofPattern("HH:mm:ss"),
                DateTimeFormatter.ISO_LOCAL_TIME
        };
        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalTime.parse(time, fmt);
            } catch (DateTimeParseException ignored) {
                // ignored
            }
        }
        throw new ResiliXException(fmt("Invalid time declaration: %s", time));
    }

    public static <T> Class<? extends T> validateAndConvertClass(String className, Class<T> type) throws ResiliXException {
        if (className == null || className.isBlank()) return null;
        try {
            Class<?> clazz = Class.forName(className);
            if (!type.isAssignableFrom(clazz)) {
                throw new ResiliXException(fmt("Class %s must implement/extend %s", className, type.getSimpleName()));
            }
            return (Class<? extends T>) clazz;
        } catch (ClassNotFoundException e) {
            throw new ResiliXException(fmt("Class not found: %s", className));
        }
    }

    public static <T> T getBean(ApplicationContext ctx, Class<T> clazz) {
        if (clazz == null) return null;
        try {
            return ctx.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            throw new ResiliXException(fmt("%s is not a Spring bean", clazz.getName()));
        }
    }

}
