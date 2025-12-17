package io.xircuitb.config;

import io.xircuitb.model.ActivePeriodConfig;
import io.xircuitb.model.SlidingWindowType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static io.xircuitb.model.SlidingWindowType.COUNT_BASED;

@Getter
@Setter
public class XircuitBYMLConfig {

    private SlidingWindowType slidingWindowType = COUNT_BASED;
    private int slidingWindowSize = 100;
    private float failureRateThreshold = 50;
    private int minNumberOfCalls = 10;
    private long waitDurationInOpenState = 5000;
    private int numCallHalfOpen = 10;
    private String activeFrom;
    private String activeTo;
    private String[] activeDays;
    private String[] exceptionsToCatch = {"java.lang.Exception"};
    private String fallbackProvider = "io.xircuitb.provider.defaults.VoidXircuitBFallbackProvider";
    private String configProvider = "io.xircuitb.provider.defaults.VoidXircuitBConfigProvider";
    private List<ActivePeriodConfig> activePeriods;
}
