package utils;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.xircuitb.provider.XircuitBFallbackProvider;
import org.springframework.stereotype.Component;

@Component
public class MockFallbackProvider implements XircuitBFallbackProvider {
    @Override
    public Object apply(CallNotPermittedException cause) {
        return "Fallback executed";
    }
}
