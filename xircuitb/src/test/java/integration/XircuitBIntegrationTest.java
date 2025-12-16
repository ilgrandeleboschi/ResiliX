package integration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xircuitb.config.XircuitBAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import util.ClockTestConfig;
import util.MockConfigProvider;
import util.MockFallbackProviderSync;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(
        classes = {XircuitBAutoConfiguration.class,
                DummyService.class,
                MockFallbackProviderSync.class,
                MockConfigProvider.class,
                ClockTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@EnableAspectJAutoProxy(proxyTargetClass = true)
class XircuitBIntegrationTest {

    @Autowired
    ClockTestConfig.MutableClock clock;
    @Autowired
    DummyService service;
    @Autowired
    CircuitBreakerRegistry registry;

    @Test
    void testCircuitBreakerTransitions() {
        assertEquals("OK", service.call(1));

        CircuitBreaker cb = registry.getAllCircuitBreakers().stream()
                .filter(item -> item.getName().equals("test"))
                .findAny()
                .orElseThrow(() -> new AssertionError("CircuitBreaker test not found"));

        assertThrows(RuntimeException.class, () -> service.call(0));
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        clock.advanceMillis(200);

        assertThrows(RuntimeException.class, () -> service.call(0));
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        clock.advanceMillis(400);

        assertEquals("OK", service.call(1));
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());

        cb.transitionToDisabledState();
    }


    @Test
    void testInactiveXircuitB() {
        assertEquals("OK", service.inactiveXircuit());

        CircuitBreaker cb = registry.getAllCircuitBreakers().stream()
                .filter(item -> item.getName().equals("inactive"))
                .findAny()
                .orElse(null);

        assertNotNull(cb);
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
        assertEquals(0, cb.getMetrics().getNumberOfFailedCalls());
    }

    @Test
    void testProvidedConf() {
        assertEquals("OK", service.providedConf());

        CircuitBreaker cb = registry.getAllCircuitBreakers().stream()
                .filter(item -> item.getName().equals("providedConf"))
                .findAny()
                .orElse(null);

        assertNotNull(cb);

        CircuitBreakerConfig config = cb.getCircuitBreakerConfig();

        assertEquals(COUNT_BASED, config.getSlidingWindowType());
        assertEquals(100, config.getSlidingWindowSize());
        assertEquals(10, config.getPermittedNumberOfCallsInHalfOpenState());
        assertEquals(10, config.getMinimumNumberOfCalls());
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    void testDoubleXb() {
        assertEquals("OK", service.doubleXb());

        assertEquals(2, registry.getAllCircuitBreakers().stream()
                .filter(item -> item.getName().equals("xb1") || item.getName().equals("xb2"))
                .count());
    }

}
