package io.resilix.model;

import lombok.Builder;
import lombok.Getter;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Builder
public class ResiliXContext {

    private Method method;
    private Object[] args;
    @Builder.Default
    private Map<String, Object> singleton = new ConcurrentHashMap<>();
    @Builder.Default
    private Map<String, Object> metadata = new ConcurrentHashMap<>();

    public <T> void putSingleton(Class<T> key, T value) {
        singleton.put(key.getName(), value);
    }

    public <T> T getSingleton(Class<T> key) {
        return key.cast(singleton.get(key.getName()));
    }

    public void put(String key, Object value) {
        metadata.put(key, value);
    }

    public <T> T get(String key, Class<T> type) {
        return type.cast(metadata.get(key));
    }

}
