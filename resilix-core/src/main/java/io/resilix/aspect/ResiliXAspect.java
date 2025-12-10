package io.resilix.aspect;

import io.resilix.pipeline.ResiliXExecutionPipeline;
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

import static io.resilix.utils.ResiliXUtils.isAsync;

@Aspect
@Component
@RequiredArgsConstructor
public class ResiliXAspect {

    private final List<ResiliXStrategy> providers;

    @Around("io.resilix.pointcut.ResiliXPointcut.allResiliX()")
    public Object applyResiliX(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();

        List<? extends ResiliXStrategy> strategies = isAsync(method) ?
                selectStrategies(method, ResiliXStrategyAsync.class) :
                selectStrategies(method, ResiliXStrategySync.class);

        return new ResiliXExecutionPipeline(strategies).execute(pjp, method);
    }

    private <T extends ResiliXStrategy> List<T> selectStrategies(Method method, Class<T> type) {
        return providers.stream()
                .filter(type::isInstance)
                .map(type::cast)
                .filter(strategy -> Arrays.stream(method.getAnnotations()).anyMatch(strategy::support))
                .sorted(Comparator.comparingInt(ResiliXStrategy::priority))
                .toList();
    }

}
