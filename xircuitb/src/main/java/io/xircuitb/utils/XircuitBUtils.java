package io.xircuitb.utils;

import io.xircuitb.exception.XircuitBConfigurationException;
import lombok.experimental.UtilityClass;

import static io.resilix.util.ResiliXUtils.fmt;

@UtilityClass
public class XircuitBUtils {

    public static void checkPositive(int value, String name) {
        if (value <= 0) {
            throw new XircuitBConfigurationException(fmt("%s must be positive", name));
        }
    }

    public static void checkPositiveNotZero(double value, String name) {
        if (value < 0) {
            throw new XircuitBConfigurationException(fmt("%s must be positive or zero", name));
        }
    }

    public static void checkBetween(double value, double from, double to, String name) {
        if (value < from || value > to) {
            throw new XircuitBConfigurationException(fmt("%s must be between %s and %s", name, from, to));
        }
    }

}
