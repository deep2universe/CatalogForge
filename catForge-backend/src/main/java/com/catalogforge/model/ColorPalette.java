package com.catalogforge.model;

/**
 * Color palette extracted from an image via LLM vision analysis.
 * All colors are hex codes (e.g., "#1a1a2e").
 */
public record ColorPalette(
    String primary,
    String secondary,
    String accent,
    String neutralLight,
    String neutralDark
) {}
