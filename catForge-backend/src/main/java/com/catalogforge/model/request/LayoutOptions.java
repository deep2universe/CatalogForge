package com.catalogforge.model.request;

/**
 * Options for text-to-layout generation.
 */
public record LayoutOptions(
    String pageFormat,
    String style,
    int variantCount,
    boolean includeSpecs,
    boolean complexStrategy
) {
    public LayoutOptions {
        if (pageFormat == null || pageFormat.isBlank()) pageFormat = "A4";
        if (variantCount <= 0) variantCount = 1;
    }

    /**
     * Create default options.
     */
    public static LayoutOptions defaults() {
        return new LayoutOptions("A4", null, 1, true, false);
    }
}
