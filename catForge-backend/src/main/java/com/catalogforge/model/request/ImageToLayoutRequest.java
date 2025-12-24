package com.catalogforge.model.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request for image-to-layout generation.
 */
public record ImageToLayoutRequest(
    @NotBlank(message = "Image URL is required")
    String imageUrl,
    
    @NotEmpty(message = "At least one product ID is required")
    List<Long> productIds,
    
    ImageLayoutOptions options
) {
    public ImageToLayoutRequest {
        productIds = productIds != null ? List.copyOf(productIds) : List.of();
        if (options == null) options = ImageLayoutOptions.defaults();
    }
}
