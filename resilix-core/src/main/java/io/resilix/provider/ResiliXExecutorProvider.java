package io.resilix.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ResiliXExecutorProvider {

    @Bean
    public ScheduledExecutorService resilixScheduler() {
        return Executors.newScheduledThreadPool(
                Runtime.getRuntime().availableProcessors(),
                Thread.ofVirtual().factory()
        );
    }

}
