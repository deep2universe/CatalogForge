package com.catalogforge.exception;

/**
 * Base exception for all CatalogForge application exceptions.
 */
public abstract class CatalogForgeException extends RuntimeException {

    protected CatalogForgeException(String message) {
        super(message);
    }

    protected CatalogForgeException(String message, Throwable cause) {
        super(message, cause);
    }
}
