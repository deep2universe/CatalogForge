package com.catalogforge.exception;

/**
 * Exception thrown when image analysis via Gemini Vision fails.
 */
public class ImageAnalysisException extends CatalogForgeException {

    public ImageAnalysisException(String message) {
        super(message);
    }

    public ImageAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
