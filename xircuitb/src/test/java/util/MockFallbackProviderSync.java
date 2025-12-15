package util;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.xircuitb.provider.XircuitBFallbackProviderSync;
import org.springframework.stereotype.Component;

@Component
public class MockFallbackProviderSync implements XircuitBFallbackProviderSync {
    @Override
    public Object apply(CallNotPermittedException cause) {
        return "Fallback executed";
    }
}
