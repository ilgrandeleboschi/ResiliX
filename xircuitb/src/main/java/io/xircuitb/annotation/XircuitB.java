package io.xircuitb.annotation;

import io.xircuitb.model.SlidingWindowType;
import io.xircuitb.provider.XircuitBConfigProvider;
import io.xircuitb.provider.XircuitBFallbackProvider;
import io.xircuitb.provider.defaults.VoidXircuitBConfigProvider;
import io.xircuitb.provider.defaults.VoidXircuitBFallbackProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.DayOfWeek;

import static io.xircuitb.model.SlidingWindowType.COUNT_BASED;

@Target(ElementType.METHOD)
@Repeatable(XircuitBs.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface XircuitB {

    String name() default "";

    SlidingWindowType slidingWindowType() default COUNT_BASED;

    int slidingWindowSize() default -1;

    float failureRateThreshold() default -1;

    int minNumberOfCalls() default -1;

    long waitDurationInOpenState() default -1;

    int numCallHalfOpen() default -1;

    String activeFrom() default "";

    String activeTo() default "";

    DayOfWeek[] activeDays() default {};

    Class<? extends XircuitBConfigProvider> configProvider() default VoidXircuitBConfigProvider.class;

    Class<? extends XircuitBFallbackProvider> fallbackProvider() default VoidXircuitBFallbackProvider.class;

    String configTemplate() default "";

    String fallbackTemplate() default "";

    Class<? extends Throwable>[] exceptionsToCatch() default {Exception.class};
}