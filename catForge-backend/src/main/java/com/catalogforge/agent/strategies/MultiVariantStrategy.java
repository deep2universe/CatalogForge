package com.catalogforge.agent.strategies;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.ParallelPipeline;
import com.catalogforge.agent.Pipeline;
import com.catalogforge.agent.PipelineStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Strategy for generating multiple layout variants in parallel.
 * Uses ParallelPipeline for concurrent generation.
 */
@Component
@Order(10) // Highest priority
public class MultiVariantStrategy implements PipelineStrategy {

    private final ParallelPipeline parallelPipeline;

    public MultiVariantStrategy(ParallelPipeline parallelPipeline) {
        this.parallelPipeline = parallelPipeline;
    }

    @Override
    public Pipeline getPipeline() {
        return parallelPipeline;
    }

    @Override
    public String name() {
        return "MultiVariant";
    }

    @Override
    public boolean matches(AgentContext context) {
        return context.isMultiVariant();
    }
}
