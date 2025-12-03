package io.xircuitb.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

@UtilityClass
public class XircuitBUtils {

    public static <T> T getOrDefault(T value, T defaultValue, Predicate<T> invalid) {
        return (value == null || invalid.test(value)) ? defaultValue : value;
    }

}
