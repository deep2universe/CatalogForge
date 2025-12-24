package com.catalogforge.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for PDF generation from a layout.
 */
public record PdfGenerationRequest(
    @NotBlank(message = "Layout ID is required")
    String layoutId,
    
    String printPreset
) {
    public PdfGenerationRequest {
        if (printPreset == null || printPreset.isBlank()) printPreset = "print-standard";
    }
}
