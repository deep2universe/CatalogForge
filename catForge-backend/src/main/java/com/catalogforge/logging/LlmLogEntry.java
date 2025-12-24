package com.catalogforge.logging;

import java.time.Instant;
import java.util.Map;

/**
 * Log entry for LLM interactions.
 * Each entry represents either a request to or response from the LLM.
 */
public record LlmLogEntry(
    String requestId,
    Instant timestamp,
    Direction direction,
    String model,
    String endpoint,
    Status status,
    Integer inputTokens,
    Integer outputTokens,
    Long durationMs,
    String promptSummary,
    String responseSummary,
    String errorMessage,
    Map<String, Object> metadata
) {
    
    public enum Direction {
        REQUEST, RESPONSE
    }
    
    public enum Status {
        SUCCESS, ERROR, TIMEOUT
    }
    
    /**
     * Creates a request log entry.
     */
    public static LlmLogEntry request(String requestId, String model, String endpoint, String promptSummary) {
        return new LlmLogEntry(
                requestId,
                Instant.now(),
                Direction.REQUEST,
                model,
                endpoint,
                null,
                null,
                null,
                null,
                promptSummary,
                null,
                null,
                null
        );
    }
    
    /**
     * Creates a success response log entry.
     */
    public static LlmLogEntry successResponse(
            String requestId, 
            String model, 
            int inputTokens, 
            int outputTokens,
            long durationMs,
            String responseSummary
    ) {
        return new LlmLogEntry(
                requestId,
                Instant.now(),
                Direction.RESPONSE,
                model,
                null,
                Status.SUCCESS,
                inputTokens,
                outputTokens,
                durationMs,
                null,
                responseSummary,
                null,
                null
        );
    }
    
    /**
     * Creates an error response log entry.
     */
    public static LlmLogEntry errorResponse(
            String requestId,
            String model,
            long durationMs,
            String errorMessage
    ) {
        return new LlmLogEntry(
                requestId,
                Instant.now(),
                Direction.RESPONSE,
                model,
                null,
                Status.ERROR,
                null,
                null,
                durationMs,
                null,
                null,
                errorMessage,
                null
        );
    }
    
    /**
     * Creates a timeout response log entry.
     */
    public static LlmLogEntry timeoutResponse(String requestId, String model, long durationMs) {
        return new LlmLogEntry(
                requestId,
                Instant.now(),
                Direction.RESPONSE,
                model,
                null,
                Status.TIMEOUT,
                null,
                null,
                durationMs,
                null,
                null,
                "Request timed out",
                null
        );
    }
    
    /**
     * Returns a copy with additional metadata.
     */
    public LlmLogEntry withMetadata(Map<String, Object> additionalMetadata) {
        return new LlmLogEntry(
                requestId, timestamp, direction, model, endpoint, status,
                inputTokens, outputTokens, durationMs, promptSummary,
                responseSummary, errorMessage, additionalMetadata
        );
    }
}
