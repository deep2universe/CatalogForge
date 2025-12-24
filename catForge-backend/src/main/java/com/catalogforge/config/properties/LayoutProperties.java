package com.catalogforge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Configuration properties for layout generation.
 */
@ConfigurationProperties(prefix = "catalogforge.layout")
@Validated
public record LayoutProperties(
    @Min(1) @Max(10)
    int variantCountDefault,
    
    @Min(1) @Max(10)
    int maxVariantCount,
    
    boolean fallbackEnabled
) {
    public LayoutProperties {
        if (variantCountDefault <= 0) variantCountDefault = 2;
        if (maxVariantCount <= 0) maxVariantCount = 5;
    }
}
