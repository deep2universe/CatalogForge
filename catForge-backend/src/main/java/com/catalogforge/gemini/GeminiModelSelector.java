package com.catalogforge.gemini;

/**
 * Selects the appropriate Gemini model based on task requirements.
 */
public class GeminiModelSelector {

    /**
     * Model for standard layout generation tasks.
     */
    public static final String MODEL_FLASH = "gemini-2.0-flash";

    /**
     * Model for complex layouts requiring more reasoning.
     */
    public static final String MODEL_PRO = "gemini-2.5-pro-preview-06-05";

    /**
     * Model for image analysis tasks.
     */
    public static final String MODEL_VISION = "gemini-2.0-flash";

    /**
     * Selects the appropriate model for layout generation.
     * 
     * @param isComplex Whether the layout is complex (multiple products, variants)
     * @param hasImage Whether the request includes image analysis
     * @return The model identifier
     */
    public static String selectModel(boolean isComplex, boolean hasImage) {
        if (hasImage) {
            return MODEL_VISION;
        }
        if (isComplex) {
            return MODEL_PRO;
        }
        return MODEL_FLASH;
    }

    /**
     * Selects model for simple text-to-layout generation.
     */
    public static String forSimpleLayout() {
        return MODEL_FLASH;
    }

    /**
     * Selects model for complex multi-variant generation.
     */
    public static String forComplexLayout() {
        return MODEL_PRO;
    }

    /**
     * Selects model for image analysis.
     */
    public static String forImageAnalysis() {
        return MODEL_VISION;
    }
}
