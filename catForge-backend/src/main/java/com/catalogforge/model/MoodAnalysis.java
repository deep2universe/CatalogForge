package com.catalogforge.model;

import java.util.List;

/**
 * Mood analysis result from LLM vision analysis.
 * Describes the visual style and feeling of an image.
 */
public record MoodAnalysis(
    String type,
    double confidence,
    List<String> keywords
) {
    public MoodAnalysis {
        keywords = keywords != null ? List.copyOf(keywords) : List.of();
    }
}
