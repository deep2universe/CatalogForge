package com.catalogforge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for Google Gemini API integration.
 */
@ConfigurationProperties(prefix = "catalogforge.gemini")
@Validated
public record GeminiProperties(
    String apiKey,
    
    @NotBlank
    String baseUrl,
    
    @NotBlank
    String modelDefault,
    
    @NotBlank
    String modelVision,
    
    @NotBlank
    String modelComplex,
    
    @Positive
    int timeoutSeconds,
    
    @Positive
    int maxRetries
) {
    public GeminiProperties {
        if (timeoutSeconds <= 0) timeoutSeconds = 60;
        if (maxRetries <= 0) maxRetries = 3;
    }
}
