package com.catalogforge.pdf;

/**
 * Print presets for PDF generation.
 * Defines quality settings for different output purposes.
 */
public enum PrintPreset {
    
    SCREEN("screen", 72, 0, false, "Screen viewing"),
    PRINT_STANDARD("print-standard", 150, 0, false, "Standard office printing"),
    PRINT_PROFESSIONAL("print-professional", 300, 3, true, "Professional printing"),
    PRINT_PREMIUM("print-premium", 300, 5, true, "Premium printing with bleed");

    private final String name;
    private final int dpi;
    private final int bleedMm;
    private final boolean cropMarks;
    private final String description;

    PrintPreset(String name, int dpi, int bleedMm, boolean cropMarks, String description) {
        this.name = name;
        this.dpi = dpi;
        this.bleedMm = bleedMm;
        this.cropMarks = cropMarks;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getDpi() {
        return dpi;
    }

    public int getBleedMm() {
        return bleedMm;
    }

    public boolean hasCropMarks() {
        return cropMarks;
    }

    public String getDescription() {
        return description;
    }

    public static PrintPreset fromName(String name) {
        for (PrintPreset preset : values()) {
            if (preset.name.equalsIgnoreCase(name)) {
                return preset;
            }
        }
        return SCREEN;
    }
}
