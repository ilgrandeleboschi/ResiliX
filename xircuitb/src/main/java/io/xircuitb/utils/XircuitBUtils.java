package io.xircuitb.utils;

import io.xircuitb.exceptions.XircuitBConfigurationException;
import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class XircuitBUtils {

    public static <T> T getOrDefault(T value, T defaultValue, Predicate<T> invalid) {
        return (value == null || invalid.test(value)) ? defaultValue : value;
    }

    public static void checkPositive(int value, String name) {
        if (value <= 0) {
            throw new XircuitBConfigurationException(name + " must be positive");
        }
    }

    public static void checkPositiveNotZero(double value, String name) {
        if (value < 0) {
            throw new XircuitBConfigurationException(name + " must be positive or zero");
        }
    }

    public static void checkBetween(double value, double from, double to, String name) {
        if (value < from || value > to) {
            throw new XircuitBConfigurationException(name + " must be between " + from + " and " + to);
        }
    }

}
