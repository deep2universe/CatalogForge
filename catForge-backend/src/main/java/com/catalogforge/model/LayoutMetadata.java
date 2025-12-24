package com.catalogforge.model;

import java.util.List;

/**
 * Metadata about a layout generation process.
 */
public record LayoutMetadata(
    List<String> skillsUsed,
    long generationTimeMs,
    int llmCallCount
) {
    public LayoutMetadata {
        skillsUsed = skillsUsed != null ? List.copyOf(skillsUsed) : List.of();
    }
}
