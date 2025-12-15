package io.xircuitb.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "default-xircuitb")
public class XircuitBDefaultPropertiesModel {

    private String slidingWindowType = "COUNT_BASED";
    private int slidingWindowSize = 100;
    private float failureRateThreshold = 50;
    private int minNumberOfCalls = 10;
    private long waitDurationInOpenState = 5000;
    private int numCallHalfOpen = 10;
    private String activeFrom = "00:00";
    private String activeTo = "23:59";
    private String[] activeDays = {"MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"};
    private String[] exceptionsToCatch = {"java.lang.Exception"};
    private String fallbackProvider = "io.xircuitb.provider.defaults.VoidXircuitBFallbackProvider";

    private List<ActivePeriodConfig> activePeriods;
}
