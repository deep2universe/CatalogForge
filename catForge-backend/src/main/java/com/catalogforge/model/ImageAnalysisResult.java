package com.catalogforge.model;

/**
 * Complete result of LLM-based image analysis.
 * Contains color palette, mood analysis, and layout hints.
 */
public record ImageAnalysisResult(
    ColorPalette colorPalette,
    MoodAnalysis mood,
    LayoutHints layoutHints
) {}
