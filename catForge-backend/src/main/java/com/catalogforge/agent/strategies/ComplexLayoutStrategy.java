package com.catalogforge.agent.strategies;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.IterativePipeline;
import com.catalogforge.agent.Pipeline;
import com.catalogforge.agent.PipelineStrategy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Strategy for complex layouts requiring validation and correction.
 * Uses IterativePipeline with retry logic.
 */
@Component
@Order(20) // Higher priority than simple
public class ComplexLayoutStrategy implements PipelineStrategy {

    private final IterativePipeline iterativePipeline;

    public ComplexLayoutStrategy(IterativePipeline iterativePipeline) {
        this.iterativePipeline = iterativePipeline;
    }

    @Override
    public Pipeline getPipeline() {
        return iterativePipeline;
    }

    @Override
    public String name() {
        return "ComplexLayout";
    }

    @Override
    public boolean matches(AgentContext context) {
        if (context.options() == null) {
            return false;
        }
        
        // Use iterative pipeline for complex strategy or when specs are included
        return context.options().complexStrategy() || context.options().includeSpecs();
    }
}
