package com.catalogforge.agent;

import com.catalogforge.agent.steps.LayoutGenerationStep;
import com.catalogforge.agent.steps.PromptAssemblyStep;
import com.catalogforge.agent.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Simple linear pipeline: PromptAssembly → LayoutGeneration → Validation.
 * Used for standard layout generation without retries or variants.
 */
@Component
public class LinearPipeline implements Pipeline {

    private static final Logger log = LoggerFactory.getLogger(LinearPipeline.class);

    private final List<AgentStep> steps;

    public LinearPipeline(
            PromptAssemblyStep promptAssemblyStep,
            LayoutGenerationStep layoutGenerationStep,
            ValidationStep validationStep
    ) {
        this.steps = List.of(promptAssemblyStep, layoutGenerationStep, validationStep);
    }

    @Override
    public AgentContext run(AgentContext context) {
        log.info("Starting LinearPipeline: {}", context.pipelineId());
        
        AgentContext current = context;
        
        for (AgentStep step : steps) {
            log.debug("Executing step: {}", step.name());
            long start = System.currentTimeMillis();
            
            current = step.execute(current);
            
            long duration = System.currentTimeMillis() - start;
            log.debug("Step {} completed in {}ms", step.name(), duration);
        }
        
        log.info("LinearPipeline completed: {} (valid={})", 
                context.pipelineId(), current.isValid());
        
        return current;
    }

    @Override
    public String name() {
        return "LinearPipeline";
    }
}
