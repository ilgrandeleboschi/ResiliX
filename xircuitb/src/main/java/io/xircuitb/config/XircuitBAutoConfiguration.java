package io.xircuitb.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.resilix.aspect.ResiliXAspect;
import io.resilix.strategy.ResiliXStrategy;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.model.XircuitBDefaultPropertiesModel;
import io.xircuitb.monitor.CircuitBreakerMonitor;
import io.xircuitb.strategy.XircuitBStrategyProvider;
import io.xircuitb.strategy.XircuitBStrategyProviderAsync;
import io.xircuitb.strategy.XircuitBStrategyProviderSync;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.time.Clock;
import java.util.List;

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
    public XircuitBStrategyProvider xircuitBStrategyProvider(CircuitBreakerRegistry registry, XircuitBConfigFactory configFactory, Clock clock) {
        return new XircuitBStrategyProvider(registry, configFactory, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBStrategyProviderSync xircuitBStrategyProviderSync(CircuitBreakerRegistry registry, XircuitBConfigFactory configFactory, Clock clock) {
        return new XircuitBStrategyProviderSync(registry, configFactory, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBStrategyProviderAsync xircuitBStrategyProviderAsync(CircuitBreakerRegistry registry, XircuitBConfigFactory configFactory, Clock clock) {
        return new XircuitBStrategyProviderAsync(registry, configFactory, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResiliXAspect resilixAspect(List<ResiliXStrategy> providers) {
        return new ResiliXAspect(providers);
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
