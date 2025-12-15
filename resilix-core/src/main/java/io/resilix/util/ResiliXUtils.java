package io.resilix.util;

import io.github.resilience4j.core.functions.CheckedSupplier;
import lombok.experimental.UtilityClass;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.scheduling.annotation.Async;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
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

    public static <T> T getOrDefault(T value, T defaultValue, Predicate<T> invalid) {
        return (value == null || invalid.test(value)) ? defaultValue : value;
    }

    public static Throwable unwrap(Throwable t) {
        while (t instanceof CompletionException || t instanceof ExecutionException) {
            t = t.getCause();
            if (t == null) break;
        }
        return t;
    }

    public static List<DayOfWeek> daysToList(DayOfWeek[] activeDays) {
        return List.of(activeDays == null ? DayOfWeek.values() : activeDays);
    }

    public static String fmt(String msg, Object... args) {
        return String.format(msg, args);
    }

}
