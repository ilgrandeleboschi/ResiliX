package utils;

import io.xircuitb.annotation.XircuitB;

import java.time.DayOfWeek;

public class Fixture {

    public static class SimpleXb {
        @XircuitB
        public String singleXb() {
            return "executed";
        }

        @XircuitB
        @XircuitB
        public String multipleXb() {
            return "executed";
        }

        @XircuitB(exceptionsToCatch = Exception.class, activeDays = DayOfWeek.MONDAY, activeFrom = "09:00", activeTo = "18:00")
        public void exceptionsAndActiveDaysInline() {
            // dummy method for annotation object
        }

        @XircuitB(fallbackProvider = MockFallbackProvider.class)
        public void fallbackProvider() {
            // dummy method for annotation object
        }
    }

    public static class ConfigProvider {
        @XircuitB(configProvider = MockConfigProvider.class)
        public void configProvider() {
            // dummy method for annotation object
        }

    }
}
