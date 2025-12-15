package util;

import io.resilix.model.ResiliXContext;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;

@UtilityClass
public class ContextBuilder {

    public static ResiliXContext defaultContext() {
        return ResiliXContext.builder().build();
    }

    public static ResiliXContext getContext(Method method) {
        return ResiliXContext.builder()
                .method(method)
                .args(new Object[0])
                .build();
    }

}