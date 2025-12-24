package com.catalogforge.model;

/**
 * A single layout variant containing generated HTML and CSS.
 */
public record LayoutVariant(
    String id,
    String html,
    String css
) {}
