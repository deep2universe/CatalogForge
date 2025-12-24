package com.catalogforge.model.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

/**
 * Request for text-to-layout generation.
 */
public record TextToLayoutRequest(
    @NotBlank(message = "Prompt is required")
    String prompt,
    
    @NotEmpty(message = "At least one product ID is required")
    List<Long> productIds,
    
    LayoutOptions options
) {
    public TextToLayoutRequest {
        productIds = productIds != null ? List.copyOf(productIds) : List.of();
        if (options == null) options = LayoutOptions.defaults();
    }
}
