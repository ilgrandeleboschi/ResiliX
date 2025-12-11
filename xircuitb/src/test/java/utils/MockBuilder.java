package utils;

import io.resilix.model.ResiliXContext;
import io.xircuitb.model.XircuitBConfigModel;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;

public class MockBuilder {

    public static final Clock FIXED_CLOCK = Clock.fixed(
            LocalDateTime.of(2024, 1, 1, 12, 0).toInstant(ZoneOffset.UTC),
            ZoneId.of("UTC")
    );

    public static XircuitBConfigModel createXircuitBConfigModel() {
        return XircuitBConfigModel.builder()
                .slidingWindowType("COUNT_BASED")
                .waitDurationInOpenState(1)
                .failureRateThreshold(50)
                .slidingWindowSize(100)
                .minNumberOfCalls(10)
                .numCallHalfOpen(10)
                .build();
    }

    public static ResiliXContext createResiliXContext(Method method) {
        return new ResiliXContext(method, new Object[0], method.getAnnotations(), Map.of());
    }

}
