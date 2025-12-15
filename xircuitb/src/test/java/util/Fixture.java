package util;

import io.xircuitb.annotation.XircuitB;

import java.time.DayOfWeek;

public class Fixture {

    public static class SimpleXb {
        @XircuitB
        public String singleXb() {
            return "executed";
        }

        @XircuitB(exceptionsToCatch = Exception.class, activeDays = DayOfWeek.MONDAY, activeFrom = "09:00", activeTo = "18:00")
        public void exceptionsAndActiveDaysInline() {
            // dummy method for annotation object
        }

    }

    public static class ConfigProvider {
        @XircuitB(configProvider = MockConfigProvider.class)
        public void configProvider() {
            // dummy method for annotation object
        }

        @XircuitB(configTemplate = "test")
        public void configTemplate() {
            // dummy method for annotation object
        }

    }

    public static class FallbackProvider {
        @XircuitB(fallbackProvider = MockFallbackProviderSync.class)
        public void fallbackProviderSync() {
            // dummy method for annotation object
        }

        @XircuitB(fallbackProvider = MockFallbackProviderAsync.class)
        public void fallbackProviderAsync() {
            // dummy method for annotation object
        }

        @XircuitB
        public void fallbackProviderVoid() {
            // dummy method for annotation object
        }

        @XircuitB(fallbackTemplate = "test")
        public void fallbackTemplate() {
            // dummy method for annotation object
        }
    }

}
