package io.resilix.execution;

import io.github.resilience4j.core.functions.CheckedSupplier;
import io.resilix.provider.ResiliXStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ResiliXExecutionPipelineTest {

    @Test
    void testPipelineExecutesStrategiesInReverseOrder() throws Throwable {
        AtomicBoolean firstApplied = new AtomicBoolean(false);
        AtomicBoolean secondApplied = new AtomicBoolean(false);

        ResiliXStrategy strategy1 = execution -> () -> {
            firstApplied.set(true);
            return execution.get();
        };

        ResiliXStrategy strategy2 = execution -> () -> {
            secondApplied.set(true);
            return execution.get();
        };

        CheckedSupplier<Object> original = () -> "result";
        ResiliXExecutionPipeline pipeline = new ResiliXExecutionPipeline(List.of(strategy1, strategy2));
        Object result = pipeline.execute(original);

        assertEquals("result", result);
        assertTrue(firstApplied.get());
        assertTrue(secondApplied.get());
    }

}