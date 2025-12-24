package com.catalogforge.exception;

/**
 * Exception thrown when PDF generation fails.
 */
public class PdfGenerationException extends CatalogForgeException {

    public PdfGenerationException(String message) {
        super(message);
    }

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
