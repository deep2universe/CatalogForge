package com.catalogforge.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.catalogforge.config.properties.LoggingProperties;

import jakarta.annotation.PostConstruct;

/**
 * Configuration for LLM interaction logging.
 * Ensures log directories exist on startup.
 */
@Configuration
public class LoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(LoggingConfig.class);

    private final LoggingProperties properties;

    public LoggingConfig(LoggingProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void init() {
        if (properties.enabled()) {
            try {
                Path logDir = Paths.get(properties.directory());
                if (!Files.exists(logDir)) {
                    Files.createDirectories(logDir);
                    log.info("Created LLM log directory: {}", logDir.toAbsolutePath());
                }
            } catch (IOException e) {
                log.warn("Failed to create LLM log directory: {}", e.getMessage());
            }
        }
    }

    public boolean isEnabled() {
        return properties.enabled();
    }

    public String getDirectory() {
        return properties.directory();
    }

    public String getFilePattern() {
        return properties.filePattern();
    }
}
