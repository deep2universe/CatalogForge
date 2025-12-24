package com.catalogforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CatalogForge Backend Application.
 * 
 * A generative AI-powered platform for creating professional product catalogs and flyers.
 * Uses Google Gemini for layout generation and image analysis.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class CatalogForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogForgeApplication.class, args);
    }
}
