package com.catalogforge.model.response;

import java.time.Instant;

/**
 * Response for image upload requests.
 */
public record ImageUploadResponse(
    String imageId,
    String imageUrl,
    Instant expiresAt
) {}
