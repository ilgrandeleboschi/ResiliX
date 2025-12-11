package io.resilix.model;

import java.lang.reflect.Method;

public record MethodKey(Class<?> targetClass, Method method) {
}
