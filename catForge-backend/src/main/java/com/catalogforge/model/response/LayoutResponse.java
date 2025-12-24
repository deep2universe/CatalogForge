package com.catalogforge.model.response;

import com.catalogforge.model.Layout;
import com.catalogforge.model.LayoutVariant;

import java.time.Instant;
import java.util.List;

/**
 * Response containing generated layout data.
 */
public record LayoutResponse(
    String id,
    String status,
    Instant generatedAt,
    String pageFormat,
    List<VariantResponse> variants,
    int variantCount
) {
    
    public record VariantResponse(
        String id,
        String html,
        String css
    ) {
        public static VariantResponse from(LayoutVariant variant) {
            return new VariantResponse(variant.id(), variant.html(), variant.css());
        }
    }
    
    public static LayoutResponse from(Layout layout) {
        List<VariantResponse> variants = layout.variants().stream()
                .map(VariantResponse::from)
                .toList();
        
        return new LayoutResponse(
                layout.id(),
                layout.status(),
                layout.generatedAt(),
                layout.pageFormat() != null ? layout.pageFormat().name() : "A4",
                variants,
                variants.size()
        );
    }
}
