package io.xircuitb.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.xircuitb.aspect.XircuitBAspect;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBDefaultPropertiesModel;
import io.xircuitb.monitor.CircuitBreakerMonitor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.time.Clock;

@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableConfigurationProperties(XircuitBDefaultPropertiesModel.class)
public class XircuitBAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XircuitBConfigFactory xircuitBConfigFactory(XircuitBDefaultPropertiesModel defaultProperties, ApplicationContext ctx) {
        return new XircuitBConfigFactory(ctx, defaultProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerMonitor circuitBreakerMonitor() {
        return new CircuitBreakerMonitor();
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBAspect xircuitBAspect(CircuitBreakerRegistry registry, XircuitBConfigFactory configFactory, Clock clock) {
        return new XircuitBAspect(registry, configFactory, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBMonitorConfig xircuitBMonitorConfig(CircuitBreakerRegistry registry, CircuitBreakerMonitor monitor) {
        return new XircuitBMonitorConfig(registry, monitor);
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
