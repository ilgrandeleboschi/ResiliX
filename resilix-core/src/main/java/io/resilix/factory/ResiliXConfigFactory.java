package io.resilix.factory;

import io.resilix.model.ResiliXConfigModel;
import io.resilix.model.ResiliXContext;
import io.resilix.provider.ResiliXFallbackProvider;

import java.lang.annotation.Annotation;

public interface ResiliXConfigFactory<A extends Annotation, C extends ResiliXConfigModel, F extends ResiliXFallbackProvider> {

    C resolveConfig(A annotation, ResiliXContext ctx);

    C fromAnnotation(A annotation, ResiliXContext ctx);

    C fromDefault(A annotation, ResiliXContext ctx);

    C merge(C base, C override);

    F resolveFallback(A annotation, ResiliXContext ctx);

}
