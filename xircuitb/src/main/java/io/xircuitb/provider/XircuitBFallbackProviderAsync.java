package io.xircuitb.provider;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

import java.util.concurrent.CompletionStage;

@FunctionalInterface
public interface XircuitBFallbackProviderAsync extends XircuitBFallbackProvider {

    CompletionStage<Object> apply(CallNotPermittedException cause);

    default CompletionStage<Object> returnFallbackModel(String xbName, CompletionStage<Object> model, CallNotPermittedException cause) {
        logOperCircuit(xbName, cause);
        return model;
    }

}
