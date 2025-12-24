package com.catalogforge.exception;

/**
 * Exception thrown when skill loading fails.
 */
public class SkillLoadException extends CatalogForgeException {

    public SkillLoadException(String message) {
        super(message);
    }

    public SkillLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
