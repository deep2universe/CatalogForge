package com.catalogforge.model.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request for updating an existing layout.
 */
public record LayoutUpdateRequest(
    @NotBlank(message = "HTML content is required")
    String html,
    
    @NotBlank(message = "CSS content is required")
    String css
) {}
