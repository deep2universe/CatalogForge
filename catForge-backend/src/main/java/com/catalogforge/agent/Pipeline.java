package com.catalogforge.agent;

/**
 * Interface for agent pipelines.
 * A pipeline orchestrates the execution of multiple steps.
 */
public interface Pipeline {
    
    /**
     * Runs the pipeline with the given context.
     * 
     * @param context The initial context
     * @return The final context after all steps
     */
    AgentContext run(AgentContext context);
    
    /**
     * Returns the name of this pipeline for logging purposes.
     */
    String name();
}
