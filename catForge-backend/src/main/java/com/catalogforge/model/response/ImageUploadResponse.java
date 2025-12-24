package com.catalogforge.model.response;

import java.time.Instant;

/**
 * Response for image upload.
 */
public record ImageUploadResponse(
    String imageId,
    String url,
    String mimeType,
    Instant expiresAt
) {}
