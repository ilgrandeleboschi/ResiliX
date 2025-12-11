package io.resilix.utils;

import io.github.resilience4j.core.functions.CheckedSupplier;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@UtilityClass
public class ResiliXUtils {

    public static boolean isAsync(Method method) {
        Class<?> returnType = method.getReturnType();
        return method.isAnnotationPresent(Async.class) ||
                CompletableFuture.class.isAssignableFrom(returnType) ||
                CompletionStage.class.isAssignableFrom(returnType) ||
                Future.class.isAssignableFrom(returnType);
    }

    public static Supplier<CompletionStage<Object>> wrapAsync(ProceedingJoinPoint pjp) {
        return () -> {
            try {
                Object ret = pjp.proceed();
                if (ret instanceof CompletionStage<?> stage) return (CompletionStage<Object>) stage;
                return CompletableFuture.completedFuture(ret);
            } catch (Throwable t) {
                CompletableFuture<Object> failed = new CompletableFuture<>();
                failed.completeExceptionally(t);
                return failed;
            }
        };
    }

    public static CheckedSupplier<Object> proceedSupplier(ProceedingJoinPoint pjp) {
        return pjp::proceed;
    }

}
