package com.catalogforge.service;

import com.catalogforge.exception.ImageUploadException;
import com.catalogforge.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for temporary image storage and management.
 */
@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);
    
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 10 * 1024 * 1024; // 10MB
    private static final int URL_VALIDITY_HOURS = 24;

    private final Map<String, StoredImage> imageStore = new ConcurrentHashMap<>();
    private final String baseUrl;

    public ImageService(@Value("${catalogforge.base-url:http://localhost:8080}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Uploads and stores an image temporarily.
     * 
     * @param base64Data Base64-encoded image data
     * @param mimeType Image MIME type
     * @param filename Original filename (optional)
     * @return The generated image ID
     */
    public String upload(String base64Data, String mimeType, String filename) {
        validateMimeType(mimeType);
        
        byte[] data = Base64.getDecoder().decode(base64Data);
        validateSize(data.length);
        
        String imageId = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(URL_VALIDITY_HOURS, ChronoUnit.HOURS);
        
        StoredImage image = new StoredImage(
                imageId,
                data,
                mimeType,
                filename,
                Instant.now(),
                expiresAt
        );
        
        imageStore.put(imageId, image);
        log.info("Image uploaded: id={}, size={} bytes, mimeType={}", 
                imageId, data.length, mimeType);
        
        return imageId;
    }

    /**
     * Generates a URL for accessing the image.
     */
    public String generateUrl(String imageId) {
        if (!imageStore.containsKey(imageId)) {
            throw new ResourceNotFoundException("Image", imageId);
        }
        return baseUrl + "/api/v1/images/" + imageId;
    }

    /**
     * Retrieves an image by ID.
     */
    public StoredImage getImage(String imageId) {
        StoredImage image = imageStore.get(imageId);
        if (image == null) {
            throw new ResourceNotFoundException("Image", imageId);
        }
        
        if (image.isExpired()) {
            imageStore.remove(imageId);
            throw new ResourceNotFoundException("Image", imageId);
        }
        
        return image;
    }

    /**
     * Retrieves image as Base64.
     */
    public String getImageBase64(String imageId) {
        StoredImage image = getImage(imageId);
        return Base64.getEncoder().encodeToString(image.data());
    }

    /**
     * Checks if an image exists and is valid.
     */
    public boolean exists(String imageId) {
        StoredImage image = imageStore.get(imageId);
        return image != null && !image.isExpired();
    }

    /**
     * Deletes an image.
     */
    public void delete(String imageId) {
        if (imageStore.remove(imageId) == null) {
            throw new ResourceNotFoundException("Image", imageId);
        }
        log.info("Image deleted: id={}", imageId);
    }

    /**
     * Scheduled cleanup of expired images.
     * Runs every hour.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void cleanup() {
        int before = imageStore.size();
        
        imageStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
        
        int removed = before - imageStore.size();
        if (removed > 0) {
            log.info("Cleaned up {} expired images", removed);
        }
    }

    /**
     * Returns the number of stored images.
     */
    public int getImageCount() {
        return imageStore.size();
    }

    private void validateMimeType(String mimeType) {
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new ImageUploadException(
                    "Invalid image type: " + mimeType + ". Allowed: " + ALLOWED_MIME_TYPES
            );
        }
    }

    private void validateSize(long size) {
        if (size > MAX_SIZE_BYTES) {
            throw new ImageUploadException(
                    "Image too large: " + size + " bytes. Maximum: " + MAX_SIZE_BYTES + " bytes"
            );
        }
    }

    /**
     * Stored image record.
     */
    public record StoredImage(
            String id,
            byte[] data,
            String mimeType,
            String filename,
            Instant uploadedAt,
            Instant expiresAt
    ) {
        public boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}
