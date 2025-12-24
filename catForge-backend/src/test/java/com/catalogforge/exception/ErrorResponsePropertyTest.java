package com.catalogforge.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.Tag;

import com.catalogforge.model.response.ErrorResponse;

import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.NotBlank;

/**
 * Property-based tests for ErrorResponse structure.
 * 
 * Property 19: Error Response Structure
 * For any error response, the body must contain: timestamp, status, error, message, and path fields.
 * 
 * Validates: Requirements 14.6
 */
@Tag("property")
@Tag("unit")
class ErrorResponsePropertyTest {

    @Property(tries = 100)
    @Label("Property 19: Error Response Structure - all fields present")
    void errorResponseContainsAllRequiredFields(
            @ForAll @IntRange(min = 400, max = 599) int status,
            @ForAll @NotBlank String error,
            @ForAll @NotBlank String message,
            @ForAll @NotBlank String path) {
        
        // When creating an error response
        ErrorResponse response = ErrorResponse.of(status, error, message, path);
        
        // Then all required fields are present and correct
        assertThat(response.timestamp()).isNotNull();
        assertThat(response.timestamp()).isBeforeOrEqualTo(Instant.now());
        assertThat(response.status()).isEqualTo(status);
        assertThat(response.error()).isEqualTo(error);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.path()).isEqualTo(path);
    }

    @Property(tries = 100)
    @Label("Property 19: Error Response Structure - timestamp is recent")
    void errorResponseTimestampIsRecent(
            @ForAll @IntRange(min = 400, max = 599) int status) {
        
        Instant before = Instant.now();
        ErrorResponse response = ErrorResponse.of(status, "Error", "Message", "/api/test");
        Instant after = Instant.now();
        
        // Timestamp should be between before and after
        assertThat(response.timestamp())
            .isAfterOrEqualTo(before)
            .isBeforeOrEqualTo(after);
    }

    @Property(tries = 50)
    @Label("Property 19: Error Response Structure - HTTP status codes valid")
    void errorResponseStatusCodesAreValid(
            @ForAll @IntRange(min = 400, max = 599) int status) {
        
        ErrorResponse response = ErrorResponse.of(status, "Error", "Message", "/api/test");
        
        // Status should be a valid HTTP error code (4xx or 5xx)
        assertThat(response.status()).isBetween(400, 599);
    }
}
