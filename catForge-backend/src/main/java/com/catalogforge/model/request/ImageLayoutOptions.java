package com.catalogforge.model.request;

/**
 * Options for image-to-layout generation.
 */
public record ImageLayoutOptions(
    String pageFormat,
    boolean extractColors,
    boolean analyzeMood,
    boolean analyzeLayout
) {
    public ImageLayoutOptions {
        if (pageFormat == null || pageFormat.isBlank()) pageFormat = "A4";
    }

    /**
     * Create default options with all analysis enabled.
     */
    public static ImageLayoutOptions defaults() {
        return new ImageLayoutOptions("A4", true, true, true);
    }
}
