package io.xircuitb.validator;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.xircuitb.exceptions.XircuitBConfigurationException;
import io.xircuitb.model.XircuitBConfigModel;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static io.xircuitb.utils.XircuitBUtils.checkBetween;
import static io.xircuitb.utils.XircuitBUtils.checkPositive;
import static io.xircuitb.utils.XircuitBUtils.checkPositiveNotZero;


@UtilityClass
public class XircuitBValidator {

    public static void validateParameters(XircuitBConfigModel config) throws XircuitBConfigurationException {
        try {
            CircuitBreakerConfig.SlidingWindowType.valueOf(config.getSlidingWindowType());
        } catch (IllegalArgumentException e) {
            throw new XircuitBConfigurationException("SlidingWindowType is not valid: " + config.getSlidingWindowType());
        }

        checkPositiveNotZero(config.getWaitDurationInOpenState(), "waitDurationInOpenState");
        checkPositive(config.getNumCallHalfOpen(), "numCallHalfOpen");
        checkPositive(config.getSlidingWindowSize(), "slidingWindowSize");
        checkPositive(config.getMinNumberOfCalls(), "minNumberOfCalls");
        checkBetween(config.getFailureRateThreshold(), 0, 100, "failureRateThreshold");
    }

    public static DayOfWeek[] validateAndConvertDays(String[] days) {
        DayOfWeek[] result = new DayOfWeek[days.length];
        for (int i = 0; i < days.length; i++) {
            try {
                result[i] = DayOfWeek.valueOf(days[i]);
            } catch (IllegalArgumentException e) {
                throw new XircuitBConfigurationException("Invalid day of week: " + days[i]);
            }
        }
        return result;
    }

    public static Class<? extends Throwable>[] validateAndConvertExceptions(String[] names) {
        Class<? extends Throwable>[] result = new Class[names.length];
        for (int i = 0; i < names.length; i++) {
            try {
                result[i] = Class.forName(names[i]).asSubclass(Throwable.class);
            } catch (ClassNotFoundException | ClassCastException e) {
                throw new XircuitBConfigurationException("Exception class is not a valid exception: " + names[i]);
            }
        }
        return result;
    }

    public static LocalTime validateAndConvertTime(String time) {
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
        throw new XircuitBConfigurationException("Invalid time declaration: " + time);
    }

    public static void validateFallbackClass(Object bean, Class<?> clazz) {
        if (!clazz.isInstance(bean)) {
            throw new XircuitBConfigurationException(bean.getClass() + " must implement " + clazz.getSimpleName());
        }
    }

}
