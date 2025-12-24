package com.catalogforge.util;

import java.util.regex.Pattern;

/**
 * Utility for sanitizing HTML content.
 * Removes potentially dangerous elements while preserving structure.
 */
public final class HtmlSanitizer {

    // Pattern to match script tags and their content
    private static final Pattern SCRIPT_PATTERN = Pattern.compile(
        "<script[^>]*>.*?</script>",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Pattern to match event handler attributes
    private static final Pattern EVENT_HANDLER_PATTERN = Pattern.compile(
        "\\s+on\\w+\\s*=\\s*([\"'][^\"']*[\"']|[^\\s>]+)",
        Pattern.CASE_INSENSITIVE
    );

    // Pattern to match javascript: URLs
    private static final Pattern JAVASCRIPT_URL_PATTERN = Pattern.compile(
        "(href|src)\\s*=\\s*[\"']?\\s*javascript:[^\"'\\s>]*[\"']?",
        Pattern.CASE_INSENSITIVE
    );

    // Pattern to match data: URLs (potential XSS vector)
    private static final Pattern DATA_URL_PATTERN = Pattern.compile(
        "(href|src)\\s*=\\s*[\"']?\\s*data:[^\"'\\s>]*[\"']?",
        Pattern.CASE_INSENSITIVE
    );

    private HtmlSanitizer() {
        // Utility class
    }

    /**
     * Sanitize HTML by removing dangerous elements.
     * Preserves structural elements and CSS classes.
     *
     * @param html the HTML to sanitize
     * @return sanitized HTML
     */
    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }

        String result = html;

        // Remove script tags and content
        result = SCRIPT_PATTERN.matcher(result).replaceAll("");

        // Remove event handlers
        result = EVENT_HANDLER_PATTERN.matcher(result).replaceAll("");

        // Remove javascript: URLs
        result = JAVASCRIPT_URL_PATTERN.matcher(result).replaceAll("");

        // Remove data: URLs
        result = DATA_URL_PATTERN.matcher(result).replaceAll("");

        return result;
    }

    /**
     * Check if HTML contains potentially dangerous content.
     *
     * @param html the HTML to check
     * @return true if dangerous content is detected
     */
    public static boolean containsDangerousContent(String html) {
        if (html == null || html.isEmpty()) {
            return false;
        }

        return SCRIPT_PATTERN.matcher(html).find()
            || EVENT_HANDLER_PATTERN.matcher(html).find()
            || JAVASCRIPT_URL_PATTERN.matcher(html).find()
            || DATA_URL_PATTERN.matcher(html).find();
    }
}
