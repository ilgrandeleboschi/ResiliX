package io.xircuitb.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "default-xircuitb")
public class XircuitBDefaultPropertiesModel {

    private String slidingWindowType;
    private int slidingWindowSize;
    private float failureRateThreshold;
    private int minNumberOfCalls;
    private long waitDurationInOpenState;
    private int numCallHalfOpen;
    private String activeFrom;
    private String activeTo;
    private String[] activeDays;
    private String[] exceptionsToCatch;
}
