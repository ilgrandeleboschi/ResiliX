package io.resilix.factory;

import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;

public interface ResiliXConfigFactory<A extends Annotation, C, F> {

    C resolveConfig(A annotation, ResiliXContext ctx);

    C fromAnnotation(A annotation, ResiliXContext ctx);

    C fromDefault(ResiliXContext ctx);

    C merge(C base, C override);

    F resolveFallback(A annotation, ResiliXContext ctx);

}
