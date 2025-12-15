package io.xircuitb.factory;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.micrometer.common.util.StringUtils;
import io.resilix.exception.ResiliXException;
import io.resilix.factory.ResiliXConfigFactory;
import io.resilix.model.ActivePeriod;
import io.resilix.model.ActiveSchedule;
import io.resilix.model.ResiliXContext;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.exception.XircuitBConfigurationException;
import io.xircuitb.model.ActivePeriodConfig;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.model.XircuitBDefaultPropertiesModel;
import io.xircuitb.provider.XircuitBConfigProvider;
import io.xircuitb.provider.XircuitBFallbackProvider;
import io.xircuitb.provider.defaults.VoidXircuitBConfigProvider;
import io.xircuitb.provider.defaults.VoidXircuitBFallbackProvider;
import io.xircuitb.registry.XircuitBConfigRegistry;
import io.xircuitb.registry.XircuitBFallbackRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;

import static io.resilix.model.ActivePeriod.buildActivePeriod;
import static io.resilix.util.ResiliXUtils.fmt;
import static io.resilix.validator.ResiliXValidator.getBean;
import static io.resilix.validator.ResiliXValidator.validateAndConvertClass;
import static io.resilix.validator.ResiliXValidator.validateAndConvertDays;
import static io.resilix.validator.ResiliXValidator.validateAndConvertExceptions;
import static org.apache.logging.log4j.util.Strings.isNotBlank;

@Slf4j
@Component
@RequiredArgsConstructor
public class XircuitBConfigFactory implements ResiliXConfigFactory<XircuitB, XircuitBConfigModel, XircuitBFallbackProvider> {

    private final ApplicationContext appCtx;
    private final XircuitBConfigRegistry configRegistry;
    private final XircuitBFallbackRegistry fallbackRegistry;
    private final XircuitBDefaultPropertiesModel defaultConf;

    @Override
    public XircuitBConfigModel resolveConfig(XircuitB xb, ResiliXContext ctx) {
        if (!StringUtils.isBlank(xb.configTemplate())) {
            XircuitBConfigModel configModel = configRegistry.get(xb.configTemplate());
            if (configModel == null)
                throw new XircuitBConfigurationException(fmt("XircuitB config template '%s' was referenced but not registered", xb.configTemplate()));
            return merge(configModel, fromAnnotation(xb, ctx));
        }
        return xb.configProvider() != VoidXircuitBConfigProvider.class ?
                merge(fromProvider(xb.configProvider()), fromAnnotation(xb, ctx)) :
                merge(fromDefault(ctx), fromAnnotation(xb, ctx));
    }

    @Override
    public XircuitBFallbackProvider resolveFallback(XircuitB xb, ResiliXContext ctx) {
        if (!StringUtils.isBlank(xb.fallbackTemplate())) {
            XircuitBFallbackProvider fallbackProvider = fallbackRegistry.get(xb.fallbackTemplate());
            if (fallbackProvider == null)
                throw new XircuitBConfigurationException(fmt("XircuitB fallback template '%s' was referenced but not registered", xb.fallbackTemplate()));
            return fallbackProvider;
        }
        if (xb.fallbackProvider() == VoidXircuitBFallbackProvider.class) return null;
        try {
            return appCtx.getBean(xb.fallbackProvider());
        } catch (NoSuchBeanDefinitionException e) {
            throw new XircuitBConfigurationException(fmt("Fallback class %s is not a Spring bean", xb.fallbackProvider().getSimpleName()));
        }
    }

    @Override
    public XircuitBConfigModel fromAnnotation(XircuitB xb, ResiliXContext ctx) throws ResiliXException {
        return XircuitBConfigModel.builder()
                .slidingWindowType(xb.slidingWindowType())
                .waitDurationInOpenState(xb.waitDurationInOpenState())
                .slidingWindowSize(xb.slidingWindowSize())
                .minNumberOfCalls(xb.minNumberOfCalls())
                .numCallHalfOpen(xb.numCallHalfOpen())
                .failureRateThreshold(xb.failureRateThreshold())
                .exceptionsToCatch(xb.exceptionsToCatch())
                .fallbackProvider(resolveFallback(xb, ctx))
                .activeSchedule(ActiveSchedule.of(buildActivePeriod(xb.activeFrom(), xb.activeTo(), xb.activeDays())))
                .build();
    }

    @Override
    public XircuitBConfigModel fromDefault(ResiliXContext ctx) {
        try {
            Class<? extends XircuitBFallbackProvider> clazz = validateAndConvertClass(defaultConf.getFallbackProvider(), XircuitBFallbackProvider.class);
            XircuitBFallbackProvider fallback = clazz == VoidXircuitBFallbackProvider.class ? null : getBean(appCtx, clazz);

            return XircuitBConfigModel.builder()
                    .slidingWindowType(defaultConf.getSlidingWindowType())
                    .slidingWindowSize(defaultConf.getSlidingWindowSize())
                    .failureRateThreshold(defaultConf.getFailureRateThreshold())
                    .minNumberOfCalls(defaultConf.getMinNumberOfCalls())
                    .waitDurationInOpenState(defaultConf.getWaitDurationInOpenState())
                    .numCallHalfOpen(defaultConf.getNumCallHalfOpen())
                    .activeSchedule(resolveActiveSchedule(defaultConf))
                    .exceptionsToCatch(validateAndConvertExceptions(defaultConf.getExceptionsToCatch()))
                    .fallbackProvider(fallback)
                    .build();
        } catch (ResiliXException e) {
            throw new XircuitBConfigurationException(e.getMessage());
        }
    }

    @Override
    public XircuitBConfigModel merge(XircuitBConfigModel base, XircuitBConfigModel override) {
        if (override == null) return base;
        return XircuitBConfigModel.builder()
                .slidingWindowType(isNotBlank(override.getSlidingWindowType()) ? override.getSlidingWindowType() : base.getSlidingWindowType())
                .slidingWindowSize(override.getSlidingWindowSize() > 0 ? override.getSlidingWindowSize() : base.getSlidingWindowSize())
                .failureRateThreshold(override.getFailureRateThreshold() > 0 ? override.getFailureRateThreshold() : base.getFailureRateThreshold())
                .minNumberOfCalls(override.getMinNumberOfCalls() > 0 ? override.getMinNumberOfCalls() : base.getMinNumberOfCalls())
                .waitDurationInOpenState(override.getWaitDurationInOpenState() > 0 ? override.getWaitDurationInOpenState() : base.getWaitDurationInOpenState())
                .numCallHalfOpen(override.getNumCallHalfOpen() > 0 ? override.getNumCallHalfOpen() : base.getNumCallHalfOpen())
                .exceptionsToCatch((override.getExceptionsToCatch() != null && override.getExceptionsToCatch().length > 0) ? override.getExceptionsToCatch() : base.getExceptionsToCatch())
                .activeSchedule(override.getActiveSchedule() != null && !override.getActiveSchedule().isEmpty() ? override.getActiveSchedule() : base.getActiveSchedule())
                .fallbackProvider(override.getFallbackProvider() != null ? override.getFallbackProvider() : base.getFallbackProvider())
                .build();
    }

    private XircuitBConfigModel fromProvider(Class<? extends XircuitBConfigProvider> config) {
        return appCtx.getBean(config).get();
    }

    public CircuitBreakerConfig buildCircuitBreakerConfig(XircuitBConfigModel cfg, Clock clock) {
        return cfg == null ? null :
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(cfg.getSlidingWindowSize())
                        .failureRateThreshold(cfg.getFailureRateThreshold())
                        .minimumNumberOfCalls(cfg.getMinNumberOfCalls())
                        .permittedNumberOfCallsInHalfOpenState(cfg.getNumCallHalfOpen())
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.valueOf(cfg.getSlidingWindowType()))
                        .waitDurationInOpenState(Duration.ofMillis(cfg.getWaitDurationInOpenState()))
                        .recordExceptions(cfg.getExceptionsToCatch())
                        .clock(clock)
                        .build();
    }

    private ActiveSchedule resolveActiveSchedule(XircuitBDefaultPropertiesModel def) {
        if (def.getActivePeriods() != null && !def.getActivePeriods().isEmpty()) {
            return new ActiveSchedule(def.getActivePeriods().stream()
                    .map(ActivePeriodConfig::toActivePeriod)
                    .toList());
        }

        ActivePeriod period = buildActivePeriod(
                def.getActiveFrom(),
                def.getActiveTo(),
                validateAndConvertDays(def.getActiveDays())
        );

        return ActiveSchedule.of(period);
    }

}
