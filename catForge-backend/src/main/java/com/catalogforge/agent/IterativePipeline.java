package com.catalogforge.agent;

import com.catalogforge.agent.steps.LayoutGenerationStep;
import com.catalogforge.agent.steps.PromptAssemblyStep;
import com.catalogforge.agent.steps.ValidationStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Iterative pipeline with validation and correction loop.
 * Retries layout generation if validation fails, up to maxRetries.
 */
@Component
public class IterativePipeline implements Pipeline {

    private static final Logger log = LoggerFactory.getLogger(IterativePipeline.class);
    private static final int DEFAULT_MAX_RETRIES = 3;

    private final PromptAssemblyStep promptAssemblyStep;
    private final LayoutGenerationStep layoutGenerationStep;
    private final ValidationStep validationStep;
    private final CorrectionStep correctionStep;
    private final FallbackStep fallbackStep;

    public IterativePipeline(
            PromptAssemblyStep promptAssemblyStep,
            LayoutGenerationStep layoutGenerationStep,
            ValidationStep validationStep,
            CorrectionStep correctionStep,
            FallbackStep fallbackStep
    ) {
        this.promptAssemblyStep = promptAssemblyStep;
        this.layoutGenerationStep = layoutGenerationStep;
        this.validationStep = validationStep;
        this.correctionStep = correctionStep;
        this.fallbackStep = fallbackStep;
    }

    @Override
    public AgentContext run(AgentContext context) {
        log.info("Starting IterativePipeline: {}", context.pipelineId());
        
        // Step 1: Assemble prompt
        AgentContext current = promptAssemblyStep.execute(context);
        
        // Step 2: Generate and validate with retry loop
        int maxRetries = DEFAULT_MAX_RETRIES;
        
        while (current.retryCount() <= maxRetries) {
            log.debug("Generation attempt {} of {}", current.retryCount() + 1, maxRetries + 1);
            
            // Generate layout
            current = layoutGenerationStep.execute(current);
            
            // Validate
            current = validationStep.execute(current);
            
            if (current.isValid()) {
                log.info("IterativePipeline completed successfully after {} attempts", 
                        current.retryCount() + 1);
                return current;
            }
            
            // If not valid and retries remaining, apply correction
            if (current.retryCount() < maxRetries) {
                log.debug("Validation failed, applying correction. Errors: {}", 
                        current.validationErrors());
                current = correctionStep.execute(current);
                current = current.withIncrementedRetry();
            } else {
                break;
            }
        }
        
        // Max retries exceeded, use fallback
        log.warn("IterativePipeline exceeded max retries, using fallback");
        current = fallbackStep.execute(current);
        
        log.info("IterativePipeline completed with fallback: {}", context.pipelineId());
        return current;
    }

    @Override
    public String name() {
        return "IterativePipeline";
    }
}
