package com.catalogforge.exception;

/**
 * Exception thrown when layout generation fails.
 */
public class LayoutGenerationException extends CatalogForgeException {

    public LayoutGenerationException(String message) {
        super(message);
    }

    public LayoutGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
