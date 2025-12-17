package io.xircuitb.validator;

import io.xircuitb.exception.XircuitBConfigurationException;
import io.xircuitb.model.XircuitBConfigModel;
import org.junit.jupiter.api.Test;

import static io.xircuitb.model.SlidingWindowType.COUNT_BASED;
import static io.xircuitb.validator.XircuitBValidator.validateParameters;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static util.XircuitBMockBuilder.createXircuitBConfigModel;

class XircuitBValidatorTest {

    @Test
    void validateParameters_noThrow() {
        assertDoesNotThrow(() -> validateParameters(createXircuitBConfigModel()));
    }

    @Test
    void validateParameters_invalidEnumValue_throw() {
        XircuitBConfigModel config = XircuitBConfigModel.builder()
                .waitDurationInOpenState(1)
                .build();

        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> validateParameters(config)
        );

        assertEquals("slidingWindowType must not be null", ex.getMessage());
    }

    @Test
    void validateParameters_waitDurationNegative_throw() {
        XircuitBConfigModel config = XircuitBConfigModel.builder()
                .slidingWindowType(COUNT_BASED)
                .waitDurationInOpenState(-1)
                .build();

        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> validateParameters(config)
        );

        assertEquals("waitDurationInOpenState must be positive or zero", ex.getMessage());
    }

    @Test
    void validateParameters_slidingWindowSizeNegative_throw() {
        XircuitBConfigModel config = XircuitBConfigModel.builder()
                .slidingWindowType(COUNT_BASED)
                .waitDurationInOpenState(1000)
                .numCallHalfOpen(10)
                .failureRateThreshold(10)
                .slidingWindowSize(-1)
                .build();

        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> validateParameters(config)
        );

        assertEquals("slidingWindowSize must be positive", ex.getMessage());
    }

    @Test
    void validateParameters_failureRateThresholdNegative_throw() {
        XircuitBConfigModel config = XircuitBConfigModel.builder()
                .slidingWindowType(COUNT_BASED)
                .waitDurationInOpenState(1000)
                .minNumberOfCalls(1)
                .numCallHalfOpen(10)
                .failureRateThreshold(105)
                .slidingWindowSize(1)
                .build();

        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> validateParameters(config)
        );

        assertEquals("failureRateThreshold must be between 0.0 and 100.0", ex.getMessage());
    }

    @Test
    void validateParameters_configNull_throw() {
        XircuitBConfigurationException e = assertThrows(XircuitBConfigurationException.class, () -> validateParameters(null));
        assertEquals("XircuitB configuration must not be null", e.getMessage());
    }

}