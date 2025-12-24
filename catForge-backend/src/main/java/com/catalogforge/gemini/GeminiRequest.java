package com.catalogforge.gemini;

import java.util.List;
import java.util.Map;

/**
 * Request structure for Gemini API calls.
 * Supports text generation with optional system instructions and response schema.
 */
public record GeminiRequest(
    List<Content> contents,
    SystemInstruction systemInstruction,
    GenerationConfig generationConfig
) {
    
    public record Content(
        List<Part> parts,
        String role
    ) {}
    
    public record Part(
        String text,
        InlineData inlineData
    ) {
        public static Part text(String text) {
            return new Part(text, null);
        }
        
        public static Part image(String mimeType, String base64Data) {
            return new Part(null, new InlineData(mimeType, base64Data));
        }
    }
    
    public record InlineData(
        String mimeType,
        String data
    ) {}
    
    public record SystemInstruction(
        List<Part> parts
    ) {
        public static SystemInstruction of(String text) {
            return new SystemInstruction(List.of(Part.text(text)));
        }
    }
    
    public record GenerationConfig(
        String responseMimeType,
        Map<String, Object> responseSchema,
        Double temperature,
        Integer maxOutputTokens
    ) {}

    /**
     * Builder for creating GeminiRequest instances.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String userPrompt;
        private String systemInstruction;
        private String responseMimeType = "application/json";
        private Map<String, Object> responseSchema;
        private Double temperature = 0.7;
        private Integer maxOutputTokens = 8192;
        private String imageBase64;
        private String imageMimeType;

        public Builder userPrompt(String prompt) {
            this.userPrompt = prompt;
            return this;
        }

        public Builder systemInstruction(String instruction) {
            this.systemInstruction = instruction;
            return this;
        }

        public Builder responseMimeType(String mimeType) {
            this.responseMimeType = mimeType;
            return this;
        }

        public Builder responseSchema(Map<String, Object> schema) {
            this.responseSchema = schema;
            return this;
        }

        public Builder temperature(Double temp) {
            this.temperature = temp;
            return this;
        }

        public Builder maxOutputTokens(Integer tokens) {
            this.maxOutputTokens = tokens;
            return this;
        }

        public Builder image(String base64Data, String mimeType) {
            this.imageBase64 = base64Data;
            this.imageMimeType = mimeType;
            return this;
        }

        public GeminiRequest build() {
            List<Part> parts = new java.util.ArrayList<>();
            
            if (imageBase64 != null && imageMimeType != null) {
                parts.add(Part.image(imageMimeType, imageBase64));
            }
            
            if (userPrompt != null) {
                parts.add(Part.text(userPrompt));
            }

            Content content = new Content(parts, "user");
            
            SystemInstruction sysInstr = systemInstruction != null 
                    ? SystemInstruction.of(systemInstruction) 
                    : null;
            
            GenerationConfig config = new GenerationConfig(
                    responseMimeType,
                    responseSchema,
                    temperature,
                    maxOutputTokens
            );

            return new GeminiRequest(List.of(content), sysInstr, config);
        }
    }
}
