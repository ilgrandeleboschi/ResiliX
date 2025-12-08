package io.resilix.aspect;

import io.resilix.execution.ResiliXExecutionPipeline;
import io.resilix.model.StrategyPriority;
import io.resilix.provider.ResiliXStrategyProvider;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
public class ResiliXAspect {

    private final List<ResiliXStrategyProvider> providers;

    @Around("io.resilix.pointcut.ResiliXPointcut.allResiliX()")
    public Object applyResiliX(ProceedingJoinPoint pjp) throws Throwable {
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        Annotation[] annotations = method.getAnnotations();

        List<StrategyPriority> strategies = providers.stream()
                .flatMap(provider -> extract(provider, annotations, method).stream())
                .sorted(Comparator.comparingInt(StrategyPriority::priority))
                .toList();

        ResiliXExecutionPipeline pipeline = new ResiliXExecutionPipeline(strategies.stream()
                .map(StrategyPriority::strategy)
                .toList()
        );

        return pipeline.execute(pjp::proceed);
    }

    private List<StrategyPriority> extract(ResiliXStrategyProvider provider, Annotation[] annotations, Method method) {
        return Arrays.stream(annotations)
                .filter(provider::support)
                .map(ann -> new StrategyPriority(provider.strategy(ann, method), provider.priority()))
                .toList();
    }

}
