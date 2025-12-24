package com.catalogforge.model;

import java.time.Instant;
import java.util.List;

/**
 * Represents a generated layout with all its variants and metadata.
 */
public record Layout(
    String id,
    String status,
    Instant generatedAt,
    PageFormat pageFormat,
    ImageAnalysisResult imageAnalysis,
    List<LayoutVariant> variants,
    LayoutMetadata metadata
) {
    public Layout {
        variants = variants != null ? List.copyOf(variants) : List.of();
    }

    /**
     * Create a new Layout with updated variants.
     */
    public Layout withVariants(List<LayoutVariant> newVariants) {
        return new Layout(id, status, generatedAt, pageFormat, imageAnalysis, newVariants, metadata);
    }
}
