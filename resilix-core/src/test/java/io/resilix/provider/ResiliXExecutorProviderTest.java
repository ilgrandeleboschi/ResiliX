package io.resilix.provider;

import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ResiliXExecutorProviderTest {

    @Test
    void testSchedulerUsesVirtualThreads() throws Exception {
        ResiliXExecutorProvider provider = new ResiliXExecutorProvider();

        try (ScheduledExecutorService scheduler = provider.resilixScheduler()) {

            Future<?> future = scheduler.submit(() -> assertTrue(Thread.currentThread().isVirtual(), "Scheduler must use virtual threads"));

            future.get();

            scheduler.shutdown();
        }
    }

}