package io.resilix.pipeline;

import io.resilix.model.MethodKey;
import io.resilix.model.ResiliXContext;
import io.resilix.strategy.ResiliXStrategy;
import io.resilix.strategy.ResiliXStrategyAsync;
import io.resilix.strategy.ResiliXStrategySync;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.resilix.util.ResiliXUtils.isAsync;

@Aspect
@Component
@RequiredArgsConstructor
public class ResiliXAspect<A extends Annotation, C, K> {

    private final List<ResiliXStrategy<A, C, K>> allStrategies;
    private final Map<MethodKey, List<ResiliXStrategy<A, C, K>>> cache = new ConcurrentHashMap<>();

    @Around("io.resilix.util.ResiliXPointcut.allResiliX()")
    public Object applyResiliX(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        MethodKey key = new MethodKey(pjp.getTarget().getClass(), method);

        List<ResiliXStrategy<A, C, K>> strategies = cache.computeIfAbsent(key, this::resolveStrategies);

        return new ResiliXExecutionPipeline<>(new ResiliXStrategyPipeline<>(strategies)).execute(
                pjp,
                ResiliXContext.builder()
                        .method(method)
                        .args(pjp.getArgs())
                        .metadata(new ConcurrentHashMap<>())
                        .build());
    }

    private List<ResiliXStrategy<A, C, K>> resolveStrategies(MethodKey key) {
        return isAsync(key.method()) ?
                selectStrategies(key.method(), ResiliXStrategyAsync.class) :
                selectStrategies(key.method(), ResiliXStrategySync.class);
    }

    private List<ResiliXStrategy<A, C, K>> selectStrategies(Method method, Class<?> type) {
        return allStrategies.stream()
                .filter(type::isInstance)
                .filter(strategy -> {
                    Class<A> annClass = strategy.support();
                    return method.getAnnotationsByType(annClass).length > 0;
                })
                .sorted(Comparator.comparingInt(ResiliXStrategy::priority))
                .toList();
    }

}
