package utils;

import io.xircuitb.model.XircuitBConfigModel;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class MockBuilder {

    public static final XircuitBConfigModel MOCKED_CONFIG = createXircuitBConfigModel();
    public static final int INVALID_FIELD_TYPE = 1;
    public final XircuitBConfigModel notStaticConfig = XircuitBConfigModel.builder().build();
    private final XircuitBConfigModel invalidAccessField = XircuitBConfigModel.builder().build();

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

    public XircuitBConfigModel notStaticConfigMethod() {
        return invalidAccessConfigMethod();
    }

    private XircuitBConfigModel invalidAccessConfigMethod() {
        return invalidAccessField;
    }

    public static int notValidReturnConfigMethod() {
        return 1;
    }

}
