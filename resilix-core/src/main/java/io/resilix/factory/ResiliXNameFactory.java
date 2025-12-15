package io.resilix.factory;

import io.resilix.model.ResiliXContext;

import java.lang.annotation.Annotation;

@FunctionalInterface
public interface ResiliXNameFactory<A extends Annotation> {

    String resolveName(A annotation, ResiliXContext ctx, int index);

}
