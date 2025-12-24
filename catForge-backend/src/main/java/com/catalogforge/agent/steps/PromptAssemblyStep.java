package com.catalogforge.agent.steps;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.AgentStep;
import com.catalogforge.model.Product;
import com.catalogforge.service.SkillsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Assembles the complete prompt from skills and product data.
 */
@Component
public class PromptAssemblyStep implements AgentStep {

    private static final Logger log = LoggerFactory.getLogger(PromptAssemblyStep.class);

    private final SkillsService skillsService;

    public PromptAssemblyStep(SkillsService skillsService) {
        this.skillsService = skillsService;
    }

    @Override
    public AgentContext execute(AgentContext context) {
        log.debug("Assembling prompt for pipeline: {}", context.pipelineId());
        
        // Assemble skills based on options
        String style = context.options() != null ? context.options().style() : null;
        String format = context.options() != null ? context.options().pageFormat() : null;
        String skillsContent = skillsService.assemblePrompt(List.of(), style, format);
        
        // Build product data section
        String productData = buildProductDataSection(context);
        
        // Build user instructions section
        String userInstructions = buildUserInstructionsSection(context);
        
        // Build image analysis section if available
        String imageSection = buildImageAnalysisSection(context);
        
        // Combine all sections
        String assembledPrompt = String.join("\n\n",
                skillsContent,
                productData,
                imageSection,
                userInstructions
        ).trim();
        
        log.debug("Assembled prompt length: {} chars", assembledPrompt.length());
        
        return context.withAssembledPrompt(assembledPrompt);
    }

    private String buildProductDataSection(AgentContext context) {
        if (context.products() == null || context.products().isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("## Product Data\n\n");
        
        for (Product product : context.products()) {
            sb.append("### ").append(product.name()).append("\n");
            sb.append("- ID: ").append(product.id()).append("\n");
            sb.append("- Category: ").append(product.category()).append("\n");
            sb.append("- Series: ").append(product.series()).append("\n");
            
            if (product.shortDescription() != null) {
                sb.append("- Short Description: ").append(product.shortDescription()).append("\n");
            }
            if (product.description() != null) {
                sb.append("- Description: ").append(product.description()).append("\n");
            }
            if (product.priceEur() != null) {
                sb.append("- Price: €").append(product.priceEur()).append("\n");
            }
            if (product.highlights() != null && !product.highlights().isEmpty()) {
                sb.append("- Highlights: ").append(String.join(", ", product.highlights())).append("\n");
            }
            if (product.specs() != null && !product.specs().specifications().isEmpty()) {
                sb.append("- Technical Data:\n");
                product.specs().specifications().forEach((key, value) -> 
                    sb.append("  - ").append(key).append(": ").append(value).append("\n")
                );
            }
            if (product.imageUrl() != null) {
                sb.append("- Image: ").append(product.imageUrl()).append("\n");
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }

    private String buildUserInstructionsSection(AgentContext context) {
        if (context.userPrompt() == null || context.userPrompt().isBlank()) {
            return "";
        }
        
        return "## User Instructions\n\n" + context.userPrompt();
    }

    private String buildImageAnalysisSection(AgentContext context) {
        if (context.imageAnalysis() == null) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("## Reference Image Analysis\n\n");
        
        var palette = context.imageAnalysis().colorPalette();
        if (palette != null) {
            sb.append("### Color Palette\n");
            sb.append("- Primary: ").append(palette.primary()).append("\n");
            sb.append("- Secondary: ").append(palette.secondary()).append("\n");
            sb.append("- Accent: ").append(palette.accent()).append("\n");
            sb.append("- Neutral Light: ").append(palette.neutralLight()).append("\n");
            sb.append("- Neutral Dark: ").append(palette.neutralDark()).append("\n\n");
        }
        
        var mood = context.imageAnalysis().mood();
        if (mood != null) {
            sb.append("### Mood Analysis\n");
            sb.append("- Type: ").append(mood.type()).append("\n");
            sb.append("- Confidence: ").append(mood.confidence()).append("\n");
            if (mood.keywords() != null && !mood.keywords().isEmpty()) {
                sb.append("- Keywords: ").append(String.join(", ", mood.keywords())).append("\n");
            }
            sb.append("\n");
        }
        
        var hints = context.imageAnalysis().layoutHints();
        if (hints != null) {
            sb.append("### Layout Hints\n");
            sb.append("- Grid Type: ").append(hints.gridType()).append("\n");
            sb.append("- Density: ").append(hints.density()).append("\n");
            sb.append("- Focus Area: ").append(hints.focusArea()).append("\n");
            sb.append("- Suggested Columns: ").append(hints.suggestedColumns()).append("\n");
        }
        
        return sb.toString();
    }

    @Override
    public String name() {
        return "PromptAssembly";
    }
}
