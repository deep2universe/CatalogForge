package com.catalogforge.model;

/**
 * Represents a page format for layout generation.
 * Dimensions are typically in millimeters for print output.
 */
public record PageFormat(
    String name,
    int width,
    int height,
    String unit
) {
    // Common page formats
    public static final PageFormat A4 = new PageFormat("A4", 210, 297, "mm");
    public static final PageFormat A5 = new PageFormat("A5", 148, 210, "mm");
    public static final PageFormat A6 = new PageFormat("A6", 105, 148, "mm");
    public static final PageFormat DL = new PageFormat("DL", 99, 210, "mm");
    public static final PageFormat SQUARE = new PageFormat("SQUARE", 210, 210, "mm");

    /**
     * Get a PageFormat by name.
     */
    public static PageFormat fromName(String name) {
        return switch (name.toUpperCase()) {
            case "A4" -> A4;
            case "A5" -> A5;
            case "A6" -> A6;
            case "DL" -> DL;
            case "SQUARE" -> SQUARE;
            default -> A4;
        };
    }
}
