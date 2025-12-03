package integration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xircuitb.config.XircuitBAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.test.context.ActiveProfiles;
import utils.MockConfigProvider;
import utils.MockFallbackProvider;

import java.time.Clock;

import static io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static utils.MockBuilder.FIXED_CLOCK;

@SpringBootTest(
        classes = {XircuitBAutoConfiguration.class,
                DummyService.class,
                MockFallbackProvider.class,
                MockConfigProvider.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@EnableAspectJAutoProxy(proxyTargetClass = true)
class XircuitBIntegrationTest {

    @Mock
    Clock clock;
    @Autowired
    DummyService service;
    @Autowired
    CircuitBreakerRegistry registry;

    @Test
    void testCircuitBreakerOpens() {
        assertEquals("OK", service.call());

        CircuitBreaker cb = registry.getAllCircuitBreakers().stream()
                .filter(item -> item.getName().equals("xbTest"))
                .findAny()
                .orElse(null);

        assertNotNull(cb);

        assertThrows(RuntimeException.class, () -> service.call());
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        assertEquals("Fallback executed", service.call());
        assertEquals(CircuitBreaker.State.OPEN, cb.getState());

        assertEquals("OK", service.call());
        assertEquals(CircuitBreaker.State.CLOSED, cb.getState());
    }

    @Test
    void testInactiveXircuitB() {
        when(clock.instant()).thenReturn(FIXED_CLOCK.instant());

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
