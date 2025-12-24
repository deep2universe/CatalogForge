package com.catalogforge.model.response;

import java.time.Instant;
import java.util.List;

import com.catalogforge.model.ImageAnalysisResult;
import com.catalogforge.model.LayoutMetadata;
import com.catalogforge.model.LayoutVariant;
import com.catalogforge.model.PageFormat;

/**
 * Response for layout generation requests.
 */
public record LayoutResponse(
    String id,
    String status,
    Instant generatedAt,
    PageFormat pageFormat,
    ImageAnalysisResult imageAnalysis,
    List<LayoutVariant> variants,
    LayoutMetadata metadata,
    boolean placeholdersUsed
) {
    public LayoutResponse {
        variants = variants != null ? List.copyOf(variants) : List.of();
    }
}
