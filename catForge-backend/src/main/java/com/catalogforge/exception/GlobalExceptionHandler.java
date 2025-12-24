package com.catalogforge.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.catalogforge.model.response.ErrorResponse;

/**
 * Global exception handler for REST API.
 * Maps exceptions to appropriate HTTP responses with ErrorResponse body.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        log.warn("Validation error: {}", ex.getMessage());
        
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .findFirst()
            .orElse("Validation failed");
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                getPath(request)
            ));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.warn("Resource not found: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                getPath(request)
            ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(
            MaxUploadSizeExceededException ex, WebRequest request) {
        
        log.warn("File too large: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(ErrorResponse.of(
                HttpStatus.PAYLOAD_TOO_LARGE.value(),
                "Payload Too Large",
                "File size exceeds maximum allowed size of 10MB",
                getPath(request)
            ));
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ErrorResponse> handleImageUpload(
            ImageUploadException ex, WebRequest request) {
        
        log.warn("Image upload error: {}", ex.getMessage());
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                getPath(request)
            ));
    }

    @ExceptionHandler({ImageAnalysisException.class, LayoutGenerationException.class})
    public ResponseEntity<ErrorResponse> handleGeminiError(
            CatalogForgeException ex, WebRequest request) {
        
        log.error("Gemini API error: {}", ex.getMessage(), ex);
        
        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(ErrorResponse.of(
                HttpStatus.BAD_GATEWAY.value(),
                "Bad Gateway",
                "External AI service error: " + ex.getMessage(),
                getPath(request)
            ));
    }

    @ExceptionHandler(PdfGenerationException.class)
    public ResponseEntity<ErrorResponse> handlePdfGeneration(
            PdfGenerationException ex, WebRequest request) {
        
        log.error("PDF generation error: {}", ex.getMessage(), ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "PDF generation failed",
                getPath(request)
            ));
    }

    @ExceptionHandler(SkillLoadException.class)
    public ResponseEntity<ErrorResponse> handleSkillLoad(
            SkillLoadException ex, WebRequest request) {
        
        log.error("Skill loading error: {}", ex.getMessage(), ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Failed to load skills",
                getPath(request)
            ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex, WebRequest request) {
        
        // Don't log as error - this is normal for favicon, dashboard, etc.
        log.debug("Static resource not found: {}", getPath(request));
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                "Resource not found",
                getPath(request)
            ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                getPath(request)
            ));
    }

    private String getPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}
