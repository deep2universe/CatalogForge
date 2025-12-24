package com.catalogforge.agent;

/**
 * Interface for a single step in the agent pipeline.
 * Each step transforms the context and passes it to the next step.
 */
@FunctionalInterface
public interface AgentStep {
    
    /**
     * Executes this step with the given context.
     * 
     * @param context The current pipeline context
     * @return The updated context after this step
     */
    AgentContext execute(AgentContext context);
    
    /**
     * Returns the name of this step for logging purposes.
     */
    default String name() {
        return getClass().getSimpleName();
    }
}
