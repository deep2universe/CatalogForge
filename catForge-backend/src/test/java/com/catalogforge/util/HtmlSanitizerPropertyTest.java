package com.catalogforge.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Tag;

import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.NotBlank;

/**
 * Property-based tests for HTML sanitization.
 * 
 * Property 16: HTML Sanitization
 * For any HTML input:
 * - Output contains no script tags
 * - Output contains no event handler attributes (onclick, onload, onerror, etc.)
 * - Structural elements (div, span, p, h1-h6, img, table) are preserved
 * - CSS classes are preserved
 * 
 * Validates: Requirements 12.1, 12.2, 12.3
 */
@Tag("property")
@Tag("unit")
class HtmlSanitizerPropertyTest {

    @Property(tries = 100)
    @Label("Property 16: HTML Sanitization - script tags removed")
    void scriptTagsAreRemoved(@ForAll @NotBlank String content) {
        String html = "<div><script>" + content + "</script></div>";
        
        String sanitized = HtmlSanitizer.sanitize(html);
        
        assertThat(sanitized).doesNotContainIgnoringCase("<script");
        assertThat(sanitized).doesNotContainIgnoringCase("</script>");
    }

    @Property(tries = 100)
    @Label("Property 16: HTML Sanitization - event handlers removed")
    void eventHandlersAreRemoved(@ForAll @NotBlank String handler) {
        String html = "<div onclick=\"" + handler + "\">Content</div>";
        
        String sanitized = HtmlSanitizer.sanitize(html);
        
        assertThat(sanitized).doesNotContainIgnoringCase("onclick");
    }

    @Property(tries = 50)
    @Label("Property 16: HTML Sanitization - structural elements preserved")
    void structuralElementsPreserved(@ForAll @NotBlank String content) {
        String html = "<div class=\"container\"><p>" + escapeHtml(content) + "</p></div>";
        
        String sanitized = HtmlSanitizer.sanitize(html);
        
        assertThat(sanitized).contains("<div");
        assertThat(sanitized).contains("<p>");
        assertThat(sanitized).contains("</p>");
        assertThat(sanitized).contains("</div>");
    }

    @Property(tries = 50)
    @Label("Property 16: HTML Sanitization - CSS classes preserved")
    void cssClassesPreserved(@ForAll @NotBlank String className) {
        // Sanitize class name to be valid CSS
        String validClass = className.replaceAll("[^a-zA-Z0-9-_]", "");
        if (validClass.isEmpty()) validClass = "test";
        
        String html = "<div class=\"" + validClass + "\">Content</div>";
        
        String sanitized = HtmlSanitizer.sanitize(html);
        
        assertThat(sanitized).contains("class=\"" + validClass + "\"");
    }

    @Property(tries = 50)
    @Label("Property 16: HTML Sanitization - javascript URLs removed")
    void javascriptUrlsRemoved(@ForAll @NotBlank String code) {
        String html = "<a href=\"javascript:" + code + "\">Link</a>";
        
        String sanitized = HtmlSanitizer.sanitize(html);
        
        assertThat(sanitized).doesNotContainIgnoringCase("javascript:");
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
