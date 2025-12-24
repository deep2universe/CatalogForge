package com.catalogforge.exception;

/**
 * Exception thrown when image upload fails.
 */
public class ImageUploadException extends CatalogForgeException {

    public ImageUploadException(String message) {
        super(message);
    }

    public ImageUploadException(String message, Throwable cause) {
        super(message, cause);
    }
}
