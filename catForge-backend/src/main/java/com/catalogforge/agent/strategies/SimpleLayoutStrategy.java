package com.catalogforge.agent.strategies;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.LinearPipeline;
import com.catalogforge.agent.Pipeline;
import com.catalogforge.agent.PipelineStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Strategy for simple, single-variant layout generation.
 * Uses LinearPipeline for straightforward generation.
 */
@Component
@Order(100) // Lower priority - fallback strategy
public class SimpleLayoutStrategy implements PipelineStrategy {

    private final LinearPipeline linearPipeline;

    public SimpleLayoutStrategy(LinearPipeline linearPipeline) {
        this.linearPipeline = linearPipeline;
    }

    @Override
    public Pipeline getPipeline() {
        return linearPipeline;
    }

    @Override
    public String name() {
        return "SimpleLayout";
    }

    @Override
    public boolean matches(AgentContext context) {
        // Default strategy - matches everything
        return true;
    }
}
