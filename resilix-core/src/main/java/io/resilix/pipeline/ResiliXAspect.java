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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.resilix.utils.ResiliXUtils.isAsync;

@Aspect
@Component
@RequiredArgsConstructor
public class ResiliXAspect {

    private final List<ResiliXStrategy> allStrategies;
    private final Map<MethodKey, List<? extends ResiliXStrategy>> cache = new ConcurrentHashMap<>();

    @Around("io.resilix.utils.ResiliXPointcut.allResiliX()")
    public Object applyResiliX(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        MethodKey key = new MethodKey(pjp.getTarget().getClass(), method);

        List<? extends ResiliXStrategy> strategies = cache.computeIfAbsent(key, this::resolveStrategies);

        return new ResiliXExecutionPipeline(new ResiliXStrategyPipeline(strategies)).execute(
                pjp,
                new ResiliXContext(method, pjp.getArgs(), method.getAnnotations(), new ConcurrentHashMap<>()
                )
        );
    }

    private List<? extends ResiliXStrategy> resolveStrategies(MethodKey key) {
        return isAsync(key.method()) ?
                selectStrategies(key.method(), ResiliXStrategyAsync.class) :
                selectStrategies(key.method(), ResiliXStrategySync.class);
    }

    private <T extends ResiliXStrategy> List<T> selectStrategies(Method method, Class<T> type) {
        return allStrategies.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .filter(strategy -> Arrays.stream(method.getAnnotations()).anyMatch(strategy::support))
                .sorted(Comparator.comparingInt(ResiliXStrategy::priority))
                .toList();
    }

}
