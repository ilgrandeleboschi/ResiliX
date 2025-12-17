package io.resilix.model;

import io.resilix.provider.ResiliXFallbackProvider;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public abstract class ResiliXConfigModel {

    @Builder.Default
    private ActiveSchedule activeSchedule = ActiveSchedule.alwaysOn();
    private Class<? extends Throwable>[] exceptionsToCatch;
    private ResiliXFallbackProvider fallbackProvider;

}
