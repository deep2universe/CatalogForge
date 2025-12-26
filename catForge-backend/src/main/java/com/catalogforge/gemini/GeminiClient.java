package com.catalogforge.gemini;

import com.catalogforge.config.properties.GeminiProperties;
import com.catalogforge.exception.LayoutGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Client for communicating with the Gemini API.
 * Handles request/response serialization and error handling.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);
    private static final String API_KEY_HEADER = "x-goog-api-key";

    private final WebClient webClient;
    private final GeminiProperties properties;

    public GeminiClient(GeminiProperties properties) {
        this.properties = properties;
        this.webClient = WebClient.builder()
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        
        log.info("GeminiClient initialized: baseUrl={}, apiKeyConfigured={}", 
                properties.baseUrl(), 
                properties.apiKey() != null && !properties.apiKey().isBlank());
    }

    /**
     * Generates content using the specified model.
     * 
     * @param model The model identifier (e.g., "gemini-2.0-flash")
     * @param request The request payload
     * @return The response from Gemini
     */
    public GeminiResponse generate(String model, GeminiRequest request) {
        String endpoint = buildEndpoint(model);
        
        log.debug("Calling Gemini API: model={}, endpoint={}", model, endpoint);
        
        try {
            GeminiResponse response = webClient.post()
                    .uri(endpoint)
                    .header(API_KEY_HEADER, properties.apiKey())
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .block();
            
            if (response == null) {
                throw new LayoutGenerationException("Empty response from Gemini API");
            }
            
            log.debug("Gemini response: success={}, tokens={}", 
                    response.isSuccess(), response.getTotalTokens());
            
            return response;
            
        } catch (WebClientResponseException e) {
            log.error("Gemini API error: status={}, body={}", 
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new LayoutGenerationException("Gemini API error: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Failed to call Gemini API", e);
            throw new LayoutGenerationException("Failed to generate content: " + e.getMessage(), e);
        }
    }

    /**
     * Generates content asynchronously.
     */
    public Mono<GeminiResponse> generateAsync(String model, GeminiRequest request) {
        String endpoint = buildEndpoint(model);
        
        return webClient.post()
                .uri(endpoint)
                .header(API_KEY_HEADER, properties.apiKey())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(GeminiResponse.class)
                .timeout(Duration.ofSeconds(properties.timeoutSeconds()));
    }

    private String buildEndpoint(String model) {
        return String.format("/models/%s:generateContent", model);
    }

    /**
     * Checks if the API key is configured.
     */
    public boolean isConfigured() {
        return properties.apiKey() != null && !properties.apiKey().isBlank();
    }
}
