package com.catalogforge.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for Puppeteer PDF generation.
 */
@ConfigurationProperties(prefix = "catalogforge.puppeteer")
@Validated
public record PuppeteerProperties(
    @NotBlank
    String nodePath,
    
    @NotBlank
    String scriptPath,
    
    @NotBlank
    String tempDir,
    
    @Positive
    int defaultDpi,
    
    @Positive
    int imageTimeoutSeconds
) {
    public PuppeteerProperties {
        if (nodePath == null || nodePath.isBlank()) nodePath = "/usr/bin/node";
        if (scriptPath == null || scriptPath.isBlank()) scriptPath = "./puppeteer/pdf-generator.js";
        if (tempDir == null || tempDir.isBlank()) tempDir = "/tmp/catalogforge/pdf";
        if (defaultDpi <= 0) defaultDpi = 300;
        if (imageTimeoutSeconds <= 0) imageTimeoutSeconds = 10;
    }
}
