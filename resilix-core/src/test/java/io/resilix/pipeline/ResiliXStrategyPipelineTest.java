package io.resilix.pipeline;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ResiliXStrategyPipelineTest {

    @Test
    void unsupportedOperationTest() {
        ResiliXStrategyPipeline resiliXStrategyPipeline = new ResiliXStrategyPipeline(null);

        assertThrows(UnsupportedOperationException.class, resiliXStrategyPipeline::priority);
        assertThrows(UnsupportedOperationException.class, () -> resiliXStrategyPipeline.support(null));
    }

}