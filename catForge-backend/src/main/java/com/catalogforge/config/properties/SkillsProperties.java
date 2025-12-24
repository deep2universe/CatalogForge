package com.catalogforge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for the Skills system.
 */
@ConfigurationProperties(prefix = "catalogforge.skills")
@Validated
public record SkillsProperties(
    @NotBlank
    String basePath,
    
    boolean cacheEnabled
) {
    public SkillsProperties {
        if (cacheEnabled == false) cacheEnabled = true;
    }
}
