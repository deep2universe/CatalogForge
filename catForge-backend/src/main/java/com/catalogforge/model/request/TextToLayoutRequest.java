package com.catalogforge.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request for text-to-layout generation.
 */
public record TextToLayoutRequest(
    @Size(min = 1, message = "At least one product ID is required")
    List<Long> productIds,
    
    LayoutOptions options,
    
    @NotBlank(message = "User prompt is required")
    @Size(max = 5000, message = "Prompt must not exceed 5000 characters")
    String prompt
) {
    public TextToLayoutRequest {
        if (productIds == null) productIds = List.of();
        if (options == null) options = LayoutOptions.defaults();
    }
}
