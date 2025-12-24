package com.catalogforge.agent;

import com.catalogforge.model.*;
import com.catalogforge.model.request.LayoutOptions;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable context passed through the agent pipeline.
 * Contains all data needed for layout generation.
 */
public record AgentContext(
    String pipelineId,
    String requestId,
    
    // Input data
    List<Product> products,
    LayoutOptions options,
    String userPrompt,
    
    // Image analysis (optional)
    String imageBase64,
    String imageMimeType,
    ImageAnalysisResult imageAnalysis,
    
    // Assembled prompt
    String assembledPrompt,
    
    // Generated output
    Layout generatedLayout,
    List<LayoutVariant> variants,
    
    // Validation state
    List<String> validationErrors,
    int retryCount,
    
    // Metadata
    Map<String, Object> metadata
) {
    
    /**
     * Creates a new context for text-to-layout generation.
     */
    public static AgentContext forTextGeneration(
            List<Product> products,
            LayoutOptions options,
            String userPrompt
    ) {
        return new AgentContext(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                products,
                options != null ? options : LayoutOptions.defaults(),
                userPrompt,
                null, null, null,
                null,
                null, List.of(),
                List.of(), 0,
                Map.of()
        );
    }
    
    /**
     * Creates a new context for image-to-layout generation.
     */
    public static AgentContext forImageGeneration(
            List<Product> products,
            LayoutOptions options,
            String userPrompt,
            String imageBase64,
            String imageMimeType
    ) {
        return new AgentContext(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                products,
                options != null ? options : LayoutOptions.defaults(),
                userPrompt,
                imageBase64, imageMimeType, null,
                null,
                null, List.of(),
                List.of(), 0,
                Map.of()
        );
    }
    
    /**
     * Returns a copy with the assembled prompt set.
     */
    public AgentContext withAssembledPrompt(String prompt) {
        return new AgentContext(
                pipelineId, requestId, products, options, userPrompt,
                imageBase64, imageMimeType, imageAnalysis,
                prompt,
                generatedLayout, variants,
                validationErrors, retryCount, metadata
        );
    }
    
    /**
     * Returns a copy with the image analysis result set.
     */
    public AgentContext withImageAnalysis(ImageAnalysisResult analysis) {
        return new AgentContext(
                pipelineId, requestId, products, options, userPrompt,
                imageBase64, imageMimeType, analysis,
                assembledPrompt,
                generatedLayout, variants,
                validationErrors, retryCount, metadata
        );
    }
    
    /**
     * Returns a copy with the generated layout set.
     */
    public AgentContext withGeneratedLayout(Layout layout) {
        return new AgentContext(
                pipelineId, requestId, products, options, userPrompt,
                imageBase64, imageMimeType, imageAnalysis,
                assembledPrompt,
                layout, variants,
                validationErrors, retryCount, metadata
        );
    }
    
    /**
     * Returns a copy with variants added.
     */
    public AgentContext withVariants(List<LayoutVariant> newVariants) {
        return new AgentContext(
                pipelineId, requestId, products, options, userPrompt,
                imageBase64, imageMimeType, imageAnalysis,
                assembledPrompt,
                generatedLayout, newVariants,
                validationErrors, retryCount, metadata
        );
    }
    
    /**
     * Returns a copy with validation errors set.
     */
    public AgentContext withValidationErrors(List<String> errors) {
        return new AgentContext(
                pipelineId, requestId, products, options, userPrompt,
                imageBase64, imageMimeType, imageAnalysis,
                assembledPrompt,
                generatedLayout, variants,
                errors, retryCount, metadata
        );
    }
    
    /**
     * Returns a copy with incremented retry count.
     */
    public AgentContext withIncrementedRetry() {
        return new AgentContext(
                pipelineId, requestId, products, options, userPrompt,
                imageBase64, imageMimeType, imageAnalysis,
                assembledPrompt,
                generatedLayout, variants,
                validationErrors, retryCount + 1, metadata
        );
    }
    
    /**
     * Returns a copy with additional metadata.
     */
    public AgentContext withMetadata(String key, Object value) {
        var newMetadata = new java.util.HashMap<>(metadata);
        newMetadata.put(key, value);
        return new AgentContext(
                pipelineId, requestId, products, options, userPrompt,
                imageBase64, imageMimeType, imageAnalysis,
                assembledPrompt,
                generatedLayout, variants,
                validationErrors, retryCount, Map.copyOf(newMetadata)
        );
    }
    
    /**
     * Checks if this context has an image for analysis.
     */
    public boolean hasImage() {
        return imageBase64 != null && !imageBase64.isBlank();
    }
    
    /**
     * Checks if validation passed (no errors).
     */
    public boolean isValid() {
        return validationErrors == null || validationErrors.isEmpty();
    }
    
    /**
     * Checks if multi-variant generation is requested.
     */
    public boolean isMultiVariant() {
        return options != null && options.variantCount() > 1;
    }
}
