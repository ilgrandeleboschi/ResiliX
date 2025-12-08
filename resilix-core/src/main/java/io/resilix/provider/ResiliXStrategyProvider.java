package io.resilix.provider;


import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public interface ResiliXStrategyProvider {

    boolean support(Annotation annotation);

    int priority();

    ResiliXStrategy strategy(Annotation annotation, Method method);

}
