package com.catalogforge.agent.steps;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.AgentStep;
import com.catalogforge.model.LayoutVariant;
import com.catalogforge.util.CssValidator;
import com.catalogforge.util.HtmlSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates and sanitizes the generated layout.
 */
@Component
public class ValidationStep implements AgentStep {

    private static final Logger log = LoggerFactory.getLogger(ValidationStep.class);

    @Override
    public AgentContext execute(AgentContext context) {
        log.debug("Validating layout for pipeline: {}", context.pipelineId());
        
        if (context.generatedLayout() == null) {
            return context.withValidationErrors(List.of("No layout generated"));
        }
        
        List<String> errors = new ArrayList<>();
        
        // Validate each variant
        List<LayoutVariant> variants = context.generatedLayout().variants();
        if (variants == null || variants.isEmpty()) {
            errors.add("No layout variants generated");
        } else {
            for (int i = 0; i < variants.size(); i++) {
                LayoutVariant variant = variants.get(i);
                validateVariant(variant, i, errors);
            }
        }
        
        if (!errors.isEmpty()) {
            log.warn("Layout validation found {} errors: {}", errors.size(), errors);
        } else {
            log.debug("Layout validation passed");
        }
        
        return context.withValidationErrors(errors);
    }

    private void validateVariant(LayoutVariant variant, int index, List<String> errors) {
        String prefix = "Variant " + index + ": ";
        
        // Validate HTML
        String html = variant.html();
        if (html == null || html.isBlank()) {
            errors.add(prefix + "HTML content is empty");
        } else {
            // Sanitize HTML
            String sanitized = HtmlSanitizer.sanitize(html);
            if (!sanitized.equals(html)) {
                log.debug("{}HTML was sanitized - potentially unsafe content removed", prefix);
            }
            
            // Check for minimum content
            if (html.length() < 50) {
                errors.add(prefix + "HTML content is too short - may be incomplete");
            }
        }
        
        // Validate CSS
        String css = variant.css();
        if (css != null && !css.isBlank()) {
            if (!CssValidator.hasBalancedBrackets(css)) {
                errors.add(prefix + "CSS has unbalanced brackets");
            }
        }
    }

    @Override
    public String name() {
        return "Validation";
    }
}
