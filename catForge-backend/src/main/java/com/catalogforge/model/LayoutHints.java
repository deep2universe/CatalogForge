package com.catalogforge.model;

/**
 * Layout hints extracted from an image via LLM vision analysis.
 * Provides suggestions for layout structure based on the reference image.
 */
public record LayoutHints(
    String gridType,
    String density,
    String focusArea,
    int suggestedColumns
) {}
