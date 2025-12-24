package com.catalogforge.agent;

/**
 * Strategy interface for selecting and configuring pipelines.
 */
public interface PipelineStrategy {
    
    /**
     * Returns the pipeline to use for this strategy.
     */
    Pipeline getPipeline();
    
    /**
     * Returns the name of this strategy.
     */
    String name();
    
    /**
     * Checks if this strategy is appropriate for the given context.
     */
    boolean matches(AgentContext context);
}
