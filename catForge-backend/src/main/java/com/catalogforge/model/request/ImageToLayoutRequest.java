package com.catalogforge.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request for image-to-layout generation.
 */
public record ImageToLayoutRequest(
    @Size(min = 1, message = "At least one product ID is required")
    List<Long> productIds,
    
    LayoutOptions options,
    
    @Size(max = 5000, message = "Prompt must not exceed 5000 characters")
    String prompt,
    
    @NotBlank(message = "Image data is required")
    String imageBase64,
    
    @NotBlank(message = "Image MIME type is required")
    String imageMimeType
) {
    public ImageToLayoutRequest {
        if (productIds == null) productIds = List.of();
        if (options == null) options = LayoutOptions.defaults();
        if (prompt == null) prompt = "";
    }
}
