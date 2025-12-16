package io.xircuitb.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.resilix.pipeline.ResiliXAspect;
import io.resilix.strategy.ResiliXStrategy;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.factory.XircuitBConfigFactory;
import io.xircuitb.factory.XircuitBNameFactory;
import io.xircuitb.model.XircuitBCacheModel;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.monitor.XircuitBMonitor;
import io.xircuitb.registry.XircuitBConfigRegistry;
import io.xircuitb.registry.XircuitBFallbackRegistry;
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
@EnableConfigurationProperties({XircuitBsYMLConfig.class, XircuitBDefaultConfig.class})
public class XircuitBAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public XircuitBConfigFactory xircuitBConfigFactory(ApplicationContext ctx, XircuitBConfigRegistry configRegistry, XircuitBFallbackRegistry fallbackRegistry, XircuitBsYMLConfig defaultProperties, XircuitBDefaultConfig defaultConfig) {
        return new XircuitBConfigFactory(ctx, configRegistry, fallbackRegistry, defaultProperties, defaultConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBNameFactory xircuitBNameFactory() {
        return new XircuitBNameFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBMonitor circuitBreakerMonitor() {
        return new XircuitBMonitor();
    }

    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBStrategyProvider xircuitBStrategyProvider(Clock clock, XircuitBConfigFactory configFactory, XircuitBNameFactory nameFactory, CircuitBreakerRegistry registry, XircuitBMonitor monitor) {
        return new XircuitBStrategyProvider(clock, configFactory, nameFactory, registry, monitor);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBConfigRegistry xircuitBConfigRegistry() {
        return new XircuitBConfigRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBFallbackRegistry xircuitBFallbackRegistry(ApplicationContext ctx) {
        return new XircuitBFallbackRegistry(ctx);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBStrategyProviderSync xircuitBStrategyProviderSync(Clock clock, XircuitBConfigFactory configFactory, XircuitBNameFactory nameFactory, CircuitBreakerRegistry registry, XircuitBMonitor monitor) {
        return new XircuitBStrategyProviderSync(clock, configFactory, nameFactory, registry, monitor);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBStrategyProviderAsync xircuitBStrategyProviderAsync(Clock clock, XircuitBConfigFactory configFactory, XircuitBNameFactory nameFactory, CircuitBreakerRegistry registry, XircuitBMonitor monitor) {
        return new XircuitBStrategyProviderAsync(clock, configFactory, nameFactory, registry, monitor);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResiliXAspect<XircuitB, XircuitBConfigModel, XircuitBCacheModel> resilixAspect(List<ResiliXStrategy<XircuitB, XircuitBConfigModel, XircuitBCacheModel>> providers) {
        return new ResiliXAspect<>(providers);
    }

    @Bean
    @ConditionalOnMissingBean
    public XircuitBMonitorConfig xircuitBMonitorConfig(CircuitBreakerRegistry registry, XircuitBMonitor monitor) {
        return new XircuitBMonitorConfig(registry, monitor);
    }

    @Bean
    @ConditionalOnMissingBean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        return CircuitBreakerRegistry.ofDefaults();
    }
}
