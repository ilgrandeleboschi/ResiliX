package integration;

import io.xircuitb.annotation.XircuitB;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import utils.MockConfigProvider;

import java.time.DayOfWeek;

@Slf4j
@Service
public class DummyService {

    @XircuitB(name = "xbTest")
    public String call(int i) {
        if (i == 0) {
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
