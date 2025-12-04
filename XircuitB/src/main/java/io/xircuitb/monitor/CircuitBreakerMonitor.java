package io.xircuitb.monitor;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CircuitBreakerMonitor {

    public void logInitParam(CircuitBreaker cb) {
        log.info("CircuitBreaker '{}' initialized with configuration: " +
                        "slidingWindowType={}, slidingWindowSize={}, minNumberOfCalls={}, " +
                        "failureRateThreshold={}, permittedNumberOfCallsInHalfOpenState={}",
                cb.getName(),
                cb.getCircuitBreakerConfig().getSlidingWindowType(),
                cb.getCircuitBreakerConfig().getSlidingWindowSize(),
                cb.getCircuitBreakerConfig().getMinimumNumberOfCalls(),
                cb.getCircuitBreakerConfig().getFailureRateThreshold(),
                cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState());
    }

    public void logStateTransition(CircuitBreakerOnStateTransitionEvent event, CircuitBreaker cb) {
        switch (event.getStateTransition()) {
            case CLOSED_TO_OPEN ->
                    log.info("CircuitBreaker '{}' transitioned from {} to {} at {} with {}% of failure rate, {} failed calls on {} total. Config failure rate is {}%",
                            event.getCircuitBreakerName(),
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState(),
                            event.getCreationTime(),
                            cb.getMetrics().getFailureRate(),
                            cb.getMetrics().getNumberOfFailedCalls(),
                            cb.getMetrics().getNumberOfBufferedCalls(),
                            cb.getCircuitBreakerConfig().getFailureRateThreshold());
            case OPEN_TO_HALF_OPEN, HALF_OPEN_TO_CLOSED ->
                    log.info("CircuitBreaker '{}' transitioned from {} to {} at {}",
                            event.getCircuitBreakerName(),
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState(),
                            event.getCreationTime());
            case HALF_OPEN_TO_OPEN ->
                    log.info("CircuitBreaker '{}' transitioned from {} to {} at {} with {}% of failure rate - Configured HALF_OPEN calls {}, {} failed calls on {} total",
                            event.getCircuitBreakerName(),
                            event.getStateTransition().getFromState(),
                            event.getStateTransition().getToState(),
                            event.getCreationTime(),
                            cb.getMetrics().getFailureRate(),
                            cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState(),
                            cb.getMetrics().getNumberOfFailedCalls(),
                            cb.getMetrics().getNumberOfBufferedCalls());
            default -> log.info("Untracked state transition");
        }
    }
}