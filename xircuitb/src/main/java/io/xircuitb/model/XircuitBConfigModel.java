package io.xircuitb.model;

import io.resilix.model.ResiliXConfigModel;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class XircuitBConfigModel extends ResiliXConfigModel {

    private SlidingWindowType slidingWindowType;
    private int slidingWindowSize;
    private float failureRateThreshold;
    private int minNumberOfCalls;
    private long waitDurationInOpenState;
    private int numCallHalfOpen;

}
