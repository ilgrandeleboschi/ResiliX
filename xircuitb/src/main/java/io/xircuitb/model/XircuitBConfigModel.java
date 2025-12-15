package io.xircuitb.model;

import io.resilix.model.ActiveSchedule;
import io.xircuitb.provider.XircuitBFallbackProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class XircuitBConfigModel {

    private String slidingWindowType;
    private int slidingWindowSize;
    private float failureRateThreshold;
    private int minNumberOfCalls;
    private long waitDurationInOpenState;
    private int numCallHalfOpen;
    private Class<? extends Throwable>[] exceptionsToCatch;
    private XircuitBFallbackProvider fallbackProvider;
    @Builder.Default
    private ActiveSchedule activeSchedule = ActiveSchedule.alwaysOn();

}
