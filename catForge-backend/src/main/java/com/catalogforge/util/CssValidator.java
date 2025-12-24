package com.catalogforge.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility for validating CSS content.
 */
public final class CssValidator {

    // Valid print units
    private static final Pattern PRINT_UNITS_PATTERN = Pattern.compile(
        "\\d+(\\.\\d+)?\\s*(mm|cm|pt|in|px|em|rem|%|vh|vw)"
    );

    private CssValidator() {
        // Utility class
    }

    /**
     * Validation result containing validity status and any warnings/errors.
     */
    public record ValidationResult(boolean isValid, List<String> errors, List<String> warnings) {
        public static ValidationResult valid() {
            return new ValidationResult(true, List.of(), List.of());
        }

        public static ValidationResult invalid(List<String> errors) {
            return new ValidationResult(false, errors, List.of());
        }

        public static ValidationResult withWarnings(List<String> warnings) {
            return new ValidationResult(true, List.of(), warnings);
        }
    }

    /**
     * Validate CSS syntax.
     *
     * @param css the CSS to validate
     * @return validation result
     */
    public static ValidationResult validate(String css) {
        if (css == null || css.isEmpty()) {
            return ValidationResult.valid();
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check for balanced brackets
        if (!hasBalancedBrackets(css)) {
            errors.add("Unbalanced brackets in CSS");
        }

        // Check for balanced parentheses
        if (!hasBalancedParentheses(css)) {
            errors.add("Unbalanced parentheses in CSS");
        }

        if (!errors.isEmpty()) {
            return ValidationResult.invalid(errors);
        }

        if (!warnings.isEmpty()) {
            return ValidationResult.withWarnings(warnings);
        }

        return ValidationResult.valid();
    }

    /**
     * Check if CSS has balanced curly brackets.
     */
    public static boolean hasBalancedBrackets(String css) {
        int count = 0;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);

            // Handle string literals
            if ((c == '"' || c == '\'') && (i == 0 || css.charAt(i - 1) != '\\')) {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }

            if (!inString) {
                if (c == '{') {
                    count++;
                } else if (c == '}') {
                    count--;
                    if (count < 0) {
                        return false;
                    }
                }
            }
        }

        return count == 0;
    }

    /**
     * Check if CSS has balanced parentheses.
     */
    public static boolean hasBalancedParentheses(String css) {
        int count = 0;
        boolean inString = false;
        char stringChar = 0;

        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);

            // Handle string literals
            if ((c == '"' || c == '\'') && (i == 0 || css.charAt(i - 1) != '\\')) {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }

            if (!inString) {
                if (c == '(') {
                    count++;
                } else if (c == ')') {
                    count--;
                    if (count < 0) {
                        return false;
                    }
                }
            }
        }

        return count == 0;
    }

    /**
     * Check if a value contains valid print units.
     */
    public static boolean containsPrintUnits(String value) {
        return PRINT_UNITS_PATTERN.matcher(value).find();
    }
}
