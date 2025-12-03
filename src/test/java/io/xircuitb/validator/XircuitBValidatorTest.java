package io.xircuitb.validator;

import io.xircuitb.exceptions.XircuitBConfigurationException;
import io.xircuitb.model.XircuitBConfigModel;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static utils.MockBuilder.createXircuitBConfigModel;

class XircuitBValidatorTest {

    @Test
    void validateParameters_noThrow() {
        assertDoesNotThrow(() -> XircuitBValidator.validateParameters(createXircuitBConfigModel()));
    }

    @Test
    void validateParameters_invalidEnumValue_throw() {
        XircuitBConfigModel config = XircuitBConfigModel.builder()
                .slidingWindowType("INVALID_TYPE")
                .waitDurationInOpenState(1)
                .build();

        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> XircuitBValidator.validateParameters(config)
        );

        assertEquals("SlidingWindowType is not valid: INVALID_TYPE", ex.getMessage());
    }

    @Test
    void validateParameters_waitDurationNegative_throw() {
        XircuitBConfigModel config = XircuitBConfigModel.builder()
                .slidingWindowType("COUNT_BASED")
                .waitDurationInOpenState(-1)
                .build();

        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> XircuitBValidator.validateParameters(config)
        );

        assertEquals("waitDurationInOpenState must be positive", ex.getMessage());
    }

    @Test
    void validateAndConvertDays_noThrow() {
        assertArrayEquals(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY}, XircuitBValidator.validateAndConvertDays(new String[]{"SUNDAY", "MONDAY"}));
    }

    @Test
    void validateAndConvertDays_invalidDay_throw() {
        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> XircuitBValidator.validateAndConvertDays(new String[]{"INVALID_DAY"})
        );

        assertEquals("Invalid day of week: INVALID_DAY", ex.getMessage());
    }

    @Test
    void validateAndConvertTime_noThrow() {
        assertEquals(LocalTime.of(9, 0), XircuitBValidator.validateAndConvertTime("09:00"));
    }

    @Test
    void validateAndConvertTime_invalidTime_throw() {
        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> XircuitBValidator.validateAndConvertTime("INVALID_TIME")
        );

        assertEquals("Invalid time declaration: INVALID_TIME", ex.getMessage());
    }

    @Test
    void validateAndConvertExceptions_noThrow() {
        assertArrayEquals(new Class[]{Exception.class}, XircuitBValidator.validateAndConvertExceptions(new String[]{"java.lang.Exception"}));
    }

    @Test
    void validateAndConvertExceptions_invalidException_throw() {
        XircuitBConfigurationException ex = assertThrows(
                XircuitBConfigurationException.class,
                () -> XircuitBValidator.validateAndConvertExceptions(new String[]{"INVALID_EXCEPTION"})
        );

        assertEquals("Exception class is not a valid exception: INVALID_EXCEPTION", ex.getMessage());
    }

}