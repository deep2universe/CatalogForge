package com.catalogforge.logging;

import com.catalogforge.gemini.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * High-level logger for LLM interactions.
 * Provides correlation between requests and responses via requestId.
 */
@Component
public class LlmInteractionLogger {

    private static final Logger log = LoggerFactory.getLogger(LlmInteractionLogger.class);
    private static final int MAX_SUMMARY_LENGTH = 200;

    private final LlmLogWriter logWriter;
    private final Map<String, Long> requestStartTimes = new ConcurrentHashMap<>();

    public LlmInteractionLogger(LlmLogWriter logWriter) {
        this.logWriter = logWriter;
    }

    /**
     * Generates a new unique request ID.
     */
    public String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Logs an outgoing request to the LLM.
     * 
     * @param requestId Unique identifier for correlation
     * @param model The model being called
     * @param endpoint The API endpoint
     * @param prompt The prompt being sent (will be summarized)
     */
    public void logRequest(String requestId, String model, String endpoint, String prompt) {
        requestStartTimes.put(requestId, System.currentTimeMillis());
        
        String promptSummary = summarize(prompt);
        LlmLogEntry entry = LlmLogEntry.request(requestId, model, endpoint, promptSummary);
        
        logWriter.write(entry);
        log.debug("LLM Request [{}]: model={}, prompt={}", requestId, model, promptSummary);
    }

    /**
     * Logs a successful response from the LLM.
     * 
     * @param requestId The request ID for correlation
     * @param model The model that responded
     * @param response The Gemini response
     */
    public void logResponse(String requestId, String model, GeminiResponse response) {
        long durationMs = calculateDuration(requestId);
        
        String responseSummary = summarize(response.getText());
        int inputTokens = response.getInputTokens();
        int outputTokens = response.getOutputTokens();
        
        LlmLogEntry entry = LlmLogEntry.successResponse(
                requestId, model, inputTokens, outputTokens, durationMs, responseSummary
        );
        
        logWriter.write(entry);
        log.debug("LLM Response [{}]: tokens={}/{}, duration={}ms", 
                requestId, inputTokens, outputTokens, durationMs);
    }

    /**
     * Logs a successful response with custom token counts.
     */
    public void logResponse(String requestId, String model, String responseText, 
                           int inputTokens, int outputTokens) {
        long durationMs = calculateDuration(requestId);
        
        String responseSummary = summarize(responseText);
        LlmLogEntry entry = LlmLogEntry.successResponse(
                requestId, model, inputTokens, outputTokens, durationMs, responseSummary
        );
        
        logWriter.write(entry);
        log.debug("LLM Response [{}]: tokens={}/{}, duration={}ms", 
                requestId, inputTokens, outputTokens, durationMs);
    }

    /**
     * Logs an error response from the LLM.
     * 
     * @param requestId The request ID for correlation
     * @param model The model that was called
     * @param error The error that occurred
     */
    public void logError(String requestId, String model, Throwable error) {
        long durationMs = calculateDuration(requestId);
        
        String errorMessage = error.getMessage();
        LlmLogEntry entry = LlmLogEntry.errorResponse(requestId, model, durationMs, errorMessage);
        
        logWriter.write(entry);
        log.warn("LLM Error [{}]: model={}, error={}, duration={}ms", 
                requestId, model, errorMessage, durationMs);
    }

    /**
     * Logs an error with a custom message.
     */
    public void logError(String requestId, String model, String errorMessage) {
        long durationMs = calculateDuration(requestId);
        
        LlmLogEntry entry = LlmLogEntry.errorResponse(requestId, model, durationMs, errorMessage);
        
        logWriter.write(entry);
        log.warn("LLM Error [{}]: model={}, error={}, duration={}ms", 
                requestId, model, errorMessage, durationMs);
    }

    /**
     * Logs a timeout.
     */
    public void logTimeout(String requestId, String model) {
        long durationMs = calculateDuration(requestId);
        
        LlmLogEntry entry = LlmLogEntry.timeoutResponse(requestId, model, durationMs);
        
        logWriter.write(entry);
        log.warn("LLM Timeout [{}]: model={}, duration={}ms", requestId, model, durationMs);
    }

    private long calculateDuration(String requestId) {
        Long startTime = requestStartTimes.remove(requestId);
        if (startTime == null) {
            return 0;
        }
        return System.currentTimeMillis() - startTime;
    }

    private String summarize(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }
        String cleaned = text.replaceAll("\\s+", " ").trim();
        if (cleaned.length() <= MAX_SUMMARY_LENGTH) {
            return cleaned;
        }
        return cleaned.substring(0, MAX_SUMMARY_LENGTH - 3) + "...";
    }
}
