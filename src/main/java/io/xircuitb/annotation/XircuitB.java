package io.xircuitb.annotation;

import io.xircuitb.provider.VoidConfigProvider;
import io.xircuitb.provider.VoidFallbackProvider;
import io.xircuitb.provider.XircuitBConfigProvider;
import io.xircuitb.provider.XircuitBFallbackProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.DayOfWeek;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(XircuitBs.class)
public @interface XircuitB {

    String name() default "";

    String slidingWindowType() default "COUNT_BASED";

    int slidingWindowSize() default -1;

    float failureRateThreshold() default -1;

    int minNumberOfCalls() default -1;

    long waitDurationInOpenState() default -1;

    int numCallHalfOpen() default -1;

    String activeFrom() default "";

    String activeTo() default "";

    DayOfWeek[] activeDays() default {};

    Class<? extends XircuitBConfigProvider> configProvider() default VoidConfigProvider.class;

    Class<? extends XircuitBFallbackProvider> fallbackProvider() default VoidFallbackProvider.class;

    Class<? extends Throwable>[] exceptionsToCatch() default {};
}