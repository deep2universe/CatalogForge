package com.catalogforge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for image handling.
 */
@ConfigurationProperties(prefix = "catalogforge.images")
@Validated
public record ImageProperties(
    @NotBlank
    String tempDir,
    
    @Positive
    int urlValidationTimeoutMs,
    
    @Positive
    int expirationHours,
    
    boolean placeholderEnabled
) {
    public ImageProperties {
        if (tempDir == null || tempDir.isBlank()) tempDir = "/tmp/catalogforge/images";
        if (urlValidationTimeoutMs <= 0) urlValidationTimeoutMs = 5000;
        if (expirationHours <= 0) expirationHours = 24;
    }
}
