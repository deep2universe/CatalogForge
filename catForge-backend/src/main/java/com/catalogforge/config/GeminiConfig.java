package com.catalogforge.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import com.catalogforge.config.properties.GeminiProperties;

/**
 * Configuration for Google Gemini API client.
 */
@Configuration
public class GeminiConfig {

    @Bean
    public WebClient geminiWebClient(GeminiProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.baseUrl())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
