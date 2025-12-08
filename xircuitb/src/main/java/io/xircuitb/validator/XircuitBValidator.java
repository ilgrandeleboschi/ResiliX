package io.xircuitb.validator;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.xircuitb.exceptions.XircuitBConfigurationException;
import io.xircuitb.model.XircuitBConfigModel;
import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static io.xircuitb.utils.XircuitBUtils.checkPositive;


@UtilityClass
public class XircuitBValidator {

    public static void validateParameters(XircuitBConfigModel config) throws XircuitBConfigurationException {
        try {
            CircuitBreakerConfig.SlidingWindowType.valueOf(config.getSlidingWindowType());
        } catch (IllegalArgumentException e) {
            throw new XircuitBConfigurationException("SlidingWindowType is not valid: " + config.getSlidingWindowType());
        }

        checkPositive(config.getWaitDurationInOpenState(), "waitDurationInOpenState");
        checkPositive(config.getNumCallHalfOpen(), "numCallHalfOpen");
        checkPositive(config.getFailureRateThreshold(), "failureRateThreshold");
        checkPositive(config.getSlidingWindowSize(), "slidingWindowSize");
        checkPositive(config.getMinNumberOfCalls(), "minNumberOfCalls");
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
        try {
            return LocalTime.parse(time);
        } catch (Exception e) {
            throw new XircuitBConfigurationException("Invalid time declaration: " + time);
        }
    }

}
