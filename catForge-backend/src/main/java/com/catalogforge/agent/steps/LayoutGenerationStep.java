package com.catalogforge.agent.steps;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.AgentStep;
import com.catalogforge.gemini.GeminiClient;
import com.catalogforge.gemini.GeminiModelSelector;
import com.catalogforge.gemini.GeminiRequest;
import com.catalogforge.gemini.GeminiResponse;
import com.catalogforge.logging.LlmInteractionLogger;
import com.catalogforge.model.Layout;
import com.catalogforge.model.LayoutMetadata;
import com.catalogforge.model.LayoutVariant;
import com.catalogforge.model.PageFormat;
import com.catalogforge.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Generates layout HTML/CSS using the Gemini API.
 */
@Component
public class LayoutGenerationStep implements AgentStep {

    private static final Logger log = LoggerFactory.getLogger(LayoutGenerationStep.class);

    private final GeminiClient geminiClient;
    private final LlmInteractionLogger llmLogger;

    public LayoutGenerationStep(GeminiClient geminiClient, LlmInteractionLogger llmLogger) {
        this.geminiClient = geminiClient;
        this.llmLogger = llmLogger;
    }

    @Override
    public AgentContext execute(AgentContext context) {
        log.debug("Generating layout for pipeline: {}", context.pipelineId());
        
        String model = selectModel(context);
        String requestId = llmLogger.generateRequestId();
        
        GeminiRequest request = buildRequest(context);
        
        llmLogger.logRequest(requestId, model, "/v1beta/models/" + model + ":generateContent", 
                summarizePrompt(context.assembledPrompt()));
        
        try {
            GeminiResponse response = geminiClient.generate(model, request);
            llmLogger.logResponse(requestId, model, response);
            
            Layout layout = parseLayoutResponse(response, context);
            return context.withGeneratedLayout(layout);
            
        } catch (Exception e) {
            llmLogger.logError(requestId, model, e);
            throw e;
        }
    }

    private String selectModel(AgentContext context) {
        if (context.options() != null && context.options().complexStrategy()) {
            return GeminiModelSelector.forComplexLayout();
        }
        return GeminiModelSelector.forSimpleLayout();
    }

    private GeminiRequest buildRequest(AgentContext context) {
        return GeminiRequest.builder()
                .systemInstruction(SYSTEM_INSTRUCTION)
                .userPrompt(context.assembledPrompt())
                .responseSchema(LAYOUT_RESPONSE_SCHEMA)
                .temperature(0.7)
                .build();
    }

    private Layout parseLayoutResponse(GeminiResponse response, AgentContext context) {
        String json = response.getText();
        if (json == null || json.isBlank()) {
            log.warn("Empty response from layout generation");
            return createFallbackLayout(context);
        }
        
        try {
            Map<String, Object> data = JsonUtils.fromJson(json, new TypeReference<>() {});
            
            String html = (String) data.getOrDefault("html", "<div>Layout generation failed</div>");
            String css = (String) data.getOrDefault("css", "");
            
            LayoutVariant variant = new LayoutVariant(
                    UUID.randomUUID().toString(),
                    html,
                    css
            );
            
            String format = context.options() != null ? context.options().pageFormat() : "A4";
            PageFormat pageFormat = new PageFormat(format, 210, 297, "mm");
            
            LayoutMetadata metadata = new LayoutMetadata(
                    List.of(), // skillsUsed
                    0L,        // generationTimeMs - will be set later
                    1          // llmCallCount
            );
            
            return new Layout(
                    UUID.randomUUID().toString(),
                    "completed",
                    Instant.now(),
                    pageFormat,
                    context.imageAnalysis(),
                    List.of(variant),
                    metadata
            );
            
        } catch (Exception e) {
            log.error("Failed to parse layout response", e);
            return createFallbackLayout(context);
        }
    }

    private Layout createFallbackLayout(AgentContext context) {
        String productNames = context.products() != null 
                ? context.products().stream()
                    .map(p -> p.name())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("Products")
                : "Products";
        
        String html = String.format("""
                <div class="fallback-layout">
                    <h1>%s</h1>
                    <p>Layout generation encountered an issue. Please try again.</p>
                </div>
                """, productNames);
        
        String css = """
                .fallback-layout {
                    padding: 2rem;
                    text-align: center;
                    font-family: system-ui, sans-serif;
                }
                """;
        
        LayoutVariant variant = new LayoutVariant(UUID.randomUUID().toString(), html, css);
        PageFormat pageFormat = new PageFormat("A4", 210, 297, "mm");
        
        return new Layout(
                UUID.randomUUID().toString(),
                "fallback",
                Instant.now(),
                pageFormat,
                null,
                List.of(variant),
                new LayoutMetadata(List.of(), 0L, 0)
        );
    }

    private String summarizePrompt(String prompt) {
        if (prompt == null) return "";
        return prompt.length() > 100 ? prompt.substring(0, 100) + "..." : prompt;
    }

    @Override
    public String name() {
        return "LayoutGeneration";
    }

    private static final String SYSTEM_INSTRUCTION = """
        You are a professional layout designer. Generate clean, semantic HTML and CSS 
        for product catalogs and marketing materials. Follow these principles:
        - Use semantic HTML5 elements
        - Create responsive, print-ready layouts
        - Apply consistent spacing and typography
        - Ensure accessibility compliance
        - Output valid JSON with 'html', 'css', and 'title' fields
        """;

    private static final Map<String, Object> LAYOUT_RESPONSE_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "html", Map.of("type", "string", "description", "The generated HTML markup"),
                    "css", Map.of("type", "string", "description", "The generated CSS styles"),
                    "title", Map.of("type", "string", "description", "A title for the layout")
            ),
            "required", java.util.List.of("html", "css", "title")
    );
}
