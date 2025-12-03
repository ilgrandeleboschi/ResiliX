package integration;

import io.xircuitb.annotation.XircuitB;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import utils.MockConfigProvider;
import utils.MockFallbackProvider;

import java.time.DayOfWeek;

@Slf4j
@Service
public class DummyService {

    private int counter = 0;

    @XircuitB(name = "xbTest",
            failureRateThreshold = 50,
            slidingWindowSize = 2,
            numCallHalfOpen = 1,
            waitDurationInOpenState = 1,
            fallbackProvider = MockFallbackProvider.class)
    public String call() {
        counter++;
        if (counter % 2 == 0) {
            throw new RuntimeException("fail");
        }
        return "OK";
    }

    @XircuitB(name = "inactive", activeDays = {DayOfWeek.SUNDAY})
    public String inactiveXircuit() {
        return "OK";
    }

    @XircuitB(name = "providedConf", configProvider = MockConfigProvider.class)
    public String providedConf() {
        return "OK";
    }

    @XircuitB(name = "xb1")
    @XircuitB(name = "xb2")
    public String doubleXb() {
        return "OK";
    }

}
