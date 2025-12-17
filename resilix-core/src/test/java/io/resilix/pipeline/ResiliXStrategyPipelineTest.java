package io.resilix.pipeline;

import io.resilix.model.ResiliXContext;
import org.junit.jupiter.api.Test;
import util.ContextBuilder;
import util.Dummy;
import util.DummyAnn;
import util.DummyCacheModel;
import util.DummyConfigModel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static util.ContextBuilder.defaultContext;

class ResiliXStrategyPipelineTest {

    @Test
    void unsupportedOperationTest() throws NoSuchMethodException {
        ResiliXStrategyPipeline<DummyAnn, DummyConfigModel, DummyCacheModel> resiliXStrategyPipeline = new ResiliXStrategyPipeline<>(null);

        DummyAnn ann = Dummy.class.getMethod("syncMethod").getAnnotation(DummyAnn.class);
        ResiliXContext ctx = defaultContext();

        assertThrows(UnsupportedOperationException.class, resiliXStrategyPipeline::priority);
        assertThrows(UnsupportedOperationException.class, resiliXStrategyPipeline::support);

        ResiliXContext finalCtx = ctx;
        assertThrows(UnsupportedOperationException.class, () -> resiliXStrategyPipeline.createConfiguration(ann, finalCtx));
        assertThrows(UnsupportedOperationException.class, () -> resiliXStrategyPipeline.computeCache("", ann, finalCtx));
        assertThrows(UnsupportedOperationException.class, () -> resiliXStrategyPipeline.resolveName(ann, finalCtx, 1));

        ctx = ContextBuilder.getContext(Dummy.class.getMethod("syncMethod"));
        assertNotNull(resiliXStrategyPipeline.extractAnnotations(ctx, DummyAnn.class));
    }

}