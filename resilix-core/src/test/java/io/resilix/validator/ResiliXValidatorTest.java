package io.resilix.validator;

import io.resilix.exception.ResiliXException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static io.resilix.validator.ResiliXValidator.getBean;
import static io.resilix.validator.ResiliXValidator.validateAndConvertClass;
import static io.resilix.validator.ResiliXValidator.validateAndConvertDays;
import static io.resilix.validator.ResiliXValidator.validateAndConvertExceptions;
import static io.resilix.validator.ResiliXValidator.validateAndConvertTime;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResiliXValidatorTest {

    @Test
    void validateAndConvertDays_noThrow() {
        assertArrayEquals(new DayOfWeek[]{DayOfWeek.SUNDAY, DayOfWeek.MONDAY}, validateAndConvertDays(new String[]{"SUNDAY", "MONDAY"}));
    }

    @Test
    void validateAndConvertDays_invalidDay_throw() {
        ResiliXException ex = assertThrows(
                ResiliXException.class,
                () -> validateAndConvertDays(new String[]{"INVALID_DAY"})
        );

        assertEquals("Invalid day of week: INVALID_DAY", ex.getMessage());
    }

    @Test
    void validateAndConvertTime_noThrow() {
        assertEquals(LocalTime.of(9, 0), validateAndConvertTime("09:00"));
    }

    @Test
    void validateAndConvertTime_invalidTime_throw() {
        ResiliXException ex = assertThrows(
                ResiliXException.class,
                () -> validateAndConvertTime("INVALID_TIME")
        );

        assertEquals("Invalid time declaration: INVALID_TIME", ex.getMessage());
    }

    @Test
    void validateAndConvertExceptions_noThrow() {
        assertArrayEquals(new Class[]{Exception.class}, validateAndConvertExceptions(new String[]{"java.lang.Exception"}));
    }

    @Test
    void validateAndConvertExceptions_invalidException_throw() {
        ResiliXException ex = assertThrows(
                ResiliXException.class,
                () -> validateAndConvertExceptions(new String[]{"INVALID_EXCEPTION"})
        );

        assertEquals("Exception class is not a valid exception: INVALID_EXCEPTION", ex.getMessage());
    }

    @Test
    void validateAndConvertClassTest() {
        assertNull(validateAndConvertClass(null, null));

        ResiliXException re = assertThrows(ResiliXException.class, () -> validateAndConvertClass("FakeClass", String.class));
        assertEquals("Class not found: FakeClass", re.getMessage());

        re = assertThrows(ResiliXException.class, () -> validateAndConvertClass("java.lang.Integer", String.class));
        assertEquals("Class java.lang.Integer must implement/extend String", re.getMessage());

        assertEquals(String.class, validateAndConvertClass("java.lang.String", String.class));
    }

    @Test
    void getBeanTest() {
        ApplicationContext ctx = mock(ApplicationContext.class);

        when(ctx.getBean(Mockito.<Class<Object>>any())).thenReturn("bean");
        assertEquals("bean", getBean(ctx, String.class));

        when(ctx.getBean(Mockito.<Class<Object>>any())).thenThrow(new NoSuchBeanDefinitionException("NoSuchBean"));
        ResiliXException re = assertThrows(ResiliXException.class, () -> getBean(ctx, String.class));
        assertEquals("java.lang.String is not a Spring bean", re.getMessage());

        assertNull(getBean(ctx, null));
    }

}