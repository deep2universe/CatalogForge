package com.catalogforge.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Applies corrections to the prompt based on validation errors.
 * Enhances the prompt with specific instructions to fix identified issues.
 */
@Component
public class CorrectionStep implements AgentStep {

    private static final Logger log = LoggerFactory.getLogger(CorrectionStep.class);

    @Override
    public AgentContext execute(AgentContext context) {
        log.debug("Applying corrections for pipeline: {}", context.pipelineId());
        
        if (context.validationErrors() == null || context.validationErrors().isEmpty()) {
            return context;
        }
        
        // Build correction instructions based on errors
        String correctionInstructions = buildCorrectionInstructions(context);
        
        // Append correction instructions to the assembled prompt
        String enhancedPrompt = context.assembledPrompt() + "\n\n" + correctionInstructions;
        
        log.debug("Added correction instructions for {} errors", context.validationErrors().size());
        
        return context
                .withAssembledPrompt(enhancedPrompt)
                .withValidationErrors(java.util.List.of()); // Clear errors for retry
    }

    private String buildCorrectionInstructions(AgentContext context) {
        StringBuilder sb = new StringBuilder();
        sb.append("## IMPORTANT: Correction Required\n\n");
        sb.append("The previous generation had the following issues that MUST be fixed:\n\n");
        
        for (String error : context.validationErrors()) {
            sb.append("- ").append(error).append("\n");
            sb.append("  → ").append(getSuggestionForError(error)).append("\n");
        }
        
        sb.append("\nPlease regenerate the layout ensuring all issues are addressed.");
        
        return sb.toString();
    }

    private String getSuggestionForError(String error) {
        if (error.contains("empty")) {
            return "Ensure the content is not empty and contains meaningful HTML/CSS";
        }
        if (error.contains("too short")) {
            return "Generate more complete content with proper structure";
        }
        if (error.contains("unbalanced brackets")) {
            return "Check CSS syntax and ensure all brackets are properly closed";
        }
        if (error.contains("sanitized")) {
            return "Avoid using script tags, event handlers, or javascript: URLs";
        }
        return "Review and fix the identified issue";
    }

    @Override
    public String name() {
        return "Correction";
    }
}
