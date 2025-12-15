package io.xircuitb.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xircuitb.monitor.XircuitBMonitor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class XircuitBMonitorConfig {

    private final CircuitBreakerRegistry registry;
    private final XircuitBMonitor monitor;

    @PostConstruct
    public void init() {
        registry.getEventPublisher().onEntryAdded(event -> {
            CircuitBreaker cb = event.getAddedEntry();
            cb.getEventPublisher().onStateTransition(e -> monitor.logStateTransition(e, cb));
        });
    }
}
