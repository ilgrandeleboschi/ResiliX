package io.xircuitb.validator;

import io.xircuitb.exception.XircuitBConfigurationException;
import io.xircuitb.model.XircuitBConfigModel;
import lombok.experimental.UtilityClass;

import static io.xircuitb.utils.XircuitBUtils.checkBetween;
import static io.xircuitb.utils.XircuitBUtils.checkPositive;
import static io.xircuitb.utils.XircuitBUtils.checkPositiveNotZero;


@UtilityClass
public class XircuitBValidator {

    public static void validateParameters(XircuitBConfigModel config) throws XircuitBConfigurationException {
        if (config == null) {
            throw new XircuitBConfigurationException("XircuitB configuration must not be null");
        }
        if (config.getSlidingWindowType() == null) {
            throw new XircuitBConfigurationException("slidingWindowType must not be null");
        }

        checkPositiveNotZero(config.getWaitDurationInOpenState(), "waitDurationInOpenState");
        checkPositive(config.getNumCallHalfOpen(), "numCallHalfOpen");
        checkPositive(config.getSlidingWindowSize(), "slidingWindowSize");
        checkPositive(config.getMinNumberOfCalls(), "minNumberOfCalls");
        checkBetween(config.getFailureRateThreshold(), 0, 100, "failureRateThreshold");
    }

}
