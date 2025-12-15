package util;

import io.resilix.model.ActivePeriod;
import io.resilix.model.ActiveSchedule;
import io.resilix.model.ResiliXContext;
import io.xircuitb.model.XircuitBConfigModel;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@UtilityClass
public class XircuitBMockBuilder {

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
                .activeSchedule(ActiveSchedule.of(new ActivePeriod(LocalTime.of(9, 0), LocalTime.of(18, 0), List.of(DayOfWeek.MONDAY))))
                .fallbackProvider(new MockFallbackProviderSync())
                .build();
    }

    public static XircuitBConfigModel createXircuitBConfigModelWithAsyncFallback() {
        return XircuitBConfigModel.builder()
                .slidingWindowType("COUNT_BASED")
                .waitDurationInOpenState(1)
                .failureRateThreshold(50)
                .slidingWindowSize(100)
                .minNumberOfCalls(10)
                .numCallHalfOpen(10)
                .activeSchedule(ActiveSchedule.of(new ActivePeriod(LocalTime.of(9, 0), LocalTime.of(18, 0), List.of(DayOfWeek.MONDAY))))
                .fallbackProvider(new MockFallbackProviderAsync())
                .build();
    }

    public static ResiliXContext createResiliXContext(Method method) {
        return ResiliXContext.builder()
                .method(method)
                .args(new Object[0])
                .metadata(new ConcurrentHashMap<>())
                .build();
    }

    public static ResiliXContext defaultResiliXContext() {
        return ResiliXContext.builder().build();
    }

}
