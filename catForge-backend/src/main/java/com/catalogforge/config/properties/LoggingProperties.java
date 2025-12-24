package com.catalogforge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Configuration properties for LLM interaction logging.
 */
@ConfigurationProperties(prefix = "catalogforge.logging.llm")
@Validated
public record LoggingProperties(
    boolean enabled,
    
    @NotBlank
    String directory,
    
    @NotBlank
    String filePattern
) {
    public LoggingProperties {
        if (directory == null || directory.isBlank()) directory = "./logs/llm";
        if (filePattern == null || filePattern.isBlank()) filePattern = "{date}_llm.jsonl";
    }
}
