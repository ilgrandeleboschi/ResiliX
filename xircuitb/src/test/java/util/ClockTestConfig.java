package util;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static util.XircuitBMockBuilder.FIXED_CLOCK;

@TestConfiguration
public class ClockTestConfig {
    @Bean
    @Primary
    public MutableClock clock() {
        return new MutableClock(FIXED_CLOCK.instant(), ZoneId.systemDefault());
    }

    public static class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        public MutableClock(Instant initialInstant, ZoneId zone) {
            this.instant = initialInstant;
            this.zone = zone;
        }

        public void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
