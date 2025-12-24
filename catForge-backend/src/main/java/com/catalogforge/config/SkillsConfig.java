package com.catalogforge.config;

import org.springframework.context.annotation.Configuration;

import com.catalogforge.config.properties.SkillsProperties;

/**
 * Configuration for the Skills system.
 * Skills are loaded from markdown files and assembled into prompts.
 */
@Configuration
public class SkillsConfig {

    private final SkillsProperties properties;

    public SkillsConfig(SkillsProperties properties) {
        this.properties = properties;
    }

    public String getBasePath() {
        return properties.basePath();
    }

    public boolean isCacheEnabled() {
        return properties.cacheEnabled();
    }
}
