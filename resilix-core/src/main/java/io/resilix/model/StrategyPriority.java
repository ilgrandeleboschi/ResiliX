package io.resilix.model;

import io.resilix.provider.ResiliXStrategy;

public record StrategyPriority(ResiliXStrategy strategy, int priority) {
}
