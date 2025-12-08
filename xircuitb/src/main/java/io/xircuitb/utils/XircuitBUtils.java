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

    public static void checkPositive(double value, String name) {
        if (value <= 0) {
            throw new XircuitBConfigurationException(name + " must be positive");
        }
    }

}
