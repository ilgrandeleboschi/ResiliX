package io.xircuitb.factory;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.xircuitb.annotation.XircuitB;
import io.xircuitb.model.XircuitBConfigModel;
import io.xircuitb.model.XircuitBDefaultPropertiesModel;
import io.xircuitb.provider.XircuitBFallbackProvider;
import io.xircuitb.provider.XircuitBFallbackProviderAsync;
import io.xircuitb.provider.XircuitBFallbackProviderSync;
import io.xircuitb.provider.defaults.VoidConfigProvider;
import io.xircuitb.provider.defaults.VoidFallbackProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;

import static io.xircuitb.utils.XircuitBUtils.getOrDefault;
import static io.xircuitb.validator.XircuitBValidator.validateAndConvertDays;
import static io.xircuitb.validator.XircuitBValidator.validateAndConvertExceptions;
import static io.xircuitb.validator.XircuitBValidator.validateAndConvertTime;
import static io.xircuitb.validator.XircuitBValidator.validateFallbackClass;

@Component
@RequiredArgsConstructor
public class XircuitBConfigFactory {

    private final ApplicationContext ctx;
    private final XircuitBDefaultPropertiesModel defaultConf;

    public XircuitBConfigModel resolveConfig(XircuitB xb) {
        return xb.configProvider() != VoidConfigProvider.class ? fromProvider(xb) : buildXircuitBConfigModel(xb);
    }

    public XircuitBFallbackProviderSync resolveSyncFallback(Class<? extends XircuitBFallbackProvider> fallback) {
        return resolveFallback(fallback, XircuitBFallbackProviderSync.class);
    }

    public XircuitBFallbackProviderAsync resolveAsyncFallback(Class<? extends XircuitBFallbackProvider> fallback) {
        return resolveFallback(fallback, XircuitBFallbackProviderAsync.class);
    }

    private <T> T resolveFallback(Class<? extends XircuitBFallbackProvider> fallback, Class<T> clazz) {
        if (fallback == VoidFallbackProvider.class) return null;
        Object bean = ctx.getBean(fallback);
        validateFallbackClass(bean, clazz);
        return clazz.cast(bean);
    }

    private XircuitBConfigModel fromProvider(XircuitB xb) {
        return ctx.getBean(xb.configProvider()).apply();
    }

    private XircuitBConfigModel buildXircuitBConfigModel(XircuitB xb) {
        return XircuitBConfigModel.builder()
                .slidingWindowType(getOrDefault(
                        xb.slidingWindowType(),
                        defaultConf.getSlidingWindowType(),
                        String::isBlank))
                .waitDurationInOpenState(getOrDefault(
                        xb.waitDurationInOpenState(),
                        defaultConf.getWaitDurationInOpenState(),
                        i -> i < 1))
                .slidingWindowSize(getOrDefault(
                        xb.slidingWindowSize(),
                        defaultConf.getSlidingWindowSize(),
                        i -> i < 1))
                .minNumberOfCalls(getOrDefault(
                        xb.minNumberOfCalls(),
                        defaultConf.getMinNumberOfCalls(),
                        i -> i < 1))
                .numCallHalfOpen(getOrDefault(
                        xb.numCallHalfOpen(),
                        defaultConf.getNumCallHalfOpen(),
                        i -> i < 1))
                .failureRateThreshold(getOrDefault(
                        xb.failureRateThreshold(),
                        defaultConf.getFailureRateThreshold(),
                        i -> i < 1))
                .activeFrom(validateAndConvertTime(
                        getOrDefault(
                                xb.activeFrom(),
                                defaultConf.getActiveFrom(),
                                String::isBlank)))
                .activeTo(validateAndConvertTime(
                        getOrDefault(
                                xb.activeTo(),
                                defaultConf.getActiveTo(),
                                String::isBlank)))
                .activeDays(xb.activeDays().length == 0 ?
                        validateAndConvertDays(defaultConf.getActiveDays()) :
                        xb.activeDays())
                .exceptionsToCatch(xb.exceptionsToCatch().length == 0 ?
                        validateAndConvertExceptions(defaultConf.getExceptionsToCatch()) :
                        xb.exceptionsToCatch())
                .build();
    }

    public CircuitBreakerConfig buildCircuitBreakerConfig(XircuitBConfigModel xircuitBConfigModel) {
        return xircuitBConfigModel == null ? null :
                CircuitBreakerConfig.custom()
                        .slidingWindowSize(xircuitBConfigModel.getSlidingWindowSize())
                        .failureRateThreshold(xircuitBConfigModel.getFailureRateThreshold())
                        .minimumNumberOfCalls(xircuitBConfigModel.getMinNumberOfCalls())
                        .permittedNumberOfCallsInHalfOpenState(xircuitBConfigModel.getNumCallHalfOpen())
                        .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.valueOf(xircuitBConfigModel.getSlidingWindowType()))
                        .waitDurationInOpenState(Duration.ofMillis(xircuitBConfigModel.getWaitDurationInOpenState()))
                        .recordExceptions(xircuitBConfigModel.getExceptionsToCatch())
                        .build();
    }

    public String resolveXbName(Method method, XircuitB xb, int index) {
        if (!xb.name().isEmpty()) {
            return xb.name();
        }

        String base = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        String sigHash = Integer.toHexString(method.toGenericString().hashCode());
        return base + "#" + sigHash + "_" + index;
    }

}
