package io.xircuitb.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

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
    private LocalTime activeFrom;
    private LocalTime activeTo;
    private DayOfWeek[] activeDays;
    private Class<? extends Throwable>[] exceptionsToCatch;

}
