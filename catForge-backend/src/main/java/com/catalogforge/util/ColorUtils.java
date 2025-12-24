package com.catalogforge.util;

import java.util.regex.Pattern;

/**
 * Utility for color validation and WCAG contrast calculations.
 */
public final class ColorUtils {

    // Pattern for 3-digit hex (#RGB)
    private static final Pattern HEX_3_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{3}$");
    
    // Pattern for 6-digit hex (#RRGGBB)
    private static final Pattern HEX_6_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private ColorUtils() {
        // Utility class
    }

    /**
     * Validate a hex color code.
     *
     * @param color the color to validate
     * @return true if valid hex color (#RGB or #RRGGBB)
     */
    public static boolean isValidHexColor(String color) {
        if (color == null || color.isEmpty()) {
            return false;
        }
        return HEX_3_PATTERN.matcher(color).matches() 
            || HEX_6_PATTERN.matcher(color).matches();
    }

    /**
     * Calculate WCAG contrast ratio between two colors.
     * 
     * @param color1 first color (hex format)
     * @param color2 second color (hex format)
     * @return contrast ratio (1.0 to 21.0)
     * @throws IllegalArgumentException if colors are invalid
     */
    public static double calculateContrastRatio(String color1, String color2) {
        if (!isValidHexColor(color1) || !isValidHexColor(color2)) {
            throw new IllegalArgumentException("Invalid hex color format");
        }

        double luminance1 = calculateRelativeLuminance(color1);
        double luminance2 = calculateRelativeLuminance(color2);

        double lighter = Math.max(luminance1, luminance2);
        double darker = Math.min(luminance1, luminance2);

        return (lighter + 0.05) / (darker + 0.05);
    }

    /**
     * Calculate relative luminance of a color.
     * Based on WCAG 2.0 formula.
     */
    public static double calculateRelativeLuminance(String hexColor) {
        int[] rgb = hexToRgb(hexColor);
        
        double r = linearize(rgb[0] / 255.0);
        double g = linearize(rgb[1] / 255.0);
        double b = linearize(rgb[2] / 255.0);

        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /**
     * Convert hex color to RGB array.
     */
    public static int[] hexToRgb(String hexColor) {
        String hex = hexColor.substring(1); // Remove #
        
        if (hex.length() == 3) {
            // Expand 3-digit to 6-digit
            hex = "" + hex.charAt(0) + hex.charAt(0) 
                     + hex.charAt(1) + hex.charAt(1) 
                     + hex.charAt(2) + hex.charAt(2);
        }

        return new int[] {
            Integer.parseInt(hex.substring(0, 2), 16),
            Integer.parseInt(hex.substring(2, 4), 16),
            Integer.parseInt(hex.substring(4, 6), 16)
        };
    }

    /**
     * Linearize a color channel value for luminance calculation.
     */
    private static double linearize(double value) {
        if (value <= 0.03928) {
            return value / 12.92;
        }
        return Math.pow((value + 0.055) / 1.055, 2.4);
    }

    /**
     * Check if contrast ratio meets WCAG AA standard for normal text (4.5:1).
     */
    public static boolean meetsWcagAA(String color1, String color2) {
        return calculateContrastRatio(color1, color2) >= 4.5;
    }

    /**
     * Check if contrast ratio meets WCAG AAA standard for normal text (7:1).
     */
    public static boolean meetsWcagAAA(String color1, String color2) {
        return calculateContrastRatio(color1, color2) >= 7.0;
    }
}
