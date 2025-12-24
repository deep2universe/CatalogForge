package com.catalogforge.model.response;

import java.time.Instant;

/**
 * Standard error response format for all API errors.
 */
public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {
    /**
     * Create an error response with current timestamp.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(Instant.now(), status, error, message, path);
    }
}
