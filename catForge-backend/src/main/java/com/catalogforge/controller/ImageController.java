package com.catalogforge.controller;

import com.catalogforge.model.response.ImageUploadResponse;
import com.catalogforge.service.ImageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

/**
 * REST controller for image upload and retrieval.
 */
@RestController
@RequestMapping("/api/v1/images")
public class ImageController {

    private static final Logger log = LoggerFactory.getLogger(ImageController.class);

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Uploads an image.
     * POST /api/v1/images/upload
     */
    @PostMapping("/upload")
    public ResponseEntity<ImageUploadResponse> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("POST /api/v1/images/upload - filename: {}, size: {} bytes", 
                file.getOriginalFilename(), file.getSize());
        
        String base64Data = Base64.getEncoder().encodeToString(file.getBytes());
        String mimeType = file.getContentType();
        String filename = file.getOriginalFilename();
        
        String imageId = imageService.upload(base64Data, mimeType, filename);
        String url = imageService.generateUrl(imageId);
        
        ImageUploadResponse response = new ImageUploadResponse(
                imageId,
                url,
                mimeType,
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Uploads an image as Base64 JSON.
     * POST /api/v1/images/upload/base64
     */
    @PostMapping("/upload/base64")
    public ResponseEntity<ImageUploadResponse> uploadImageBase64(@RequestBody ImageUploadRequest request) {
        log.info("POST /api/v1/images/upload/base64 - mimeType: {}", request.mimeType());
        
        String imageId = imageService.upload(request.base64Data(), request.mimeType(), request.filename());
        String url = imageService.generateUrl(imageId);
        
        ImageUploadResponse response = new ImageUploadResponse(
                imageId,
                url,
                request.mimeType(),
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves an image by ID.
     * GET /api/v1/images/{imageId}
     */
    @GetMapping("/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String imageId) {
        log.debug("GET /api/v1/images/{}", imageId);
        
        ImageService.StoredImage image = imageService.getImage(imageId);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(image.mimeType()));
        headers.setContentLength(image.data().length);
        
        return ResponseEntity.ok().headers(headers).body(image.data());
    }

    /**
     * Deletes an image.
     * DELETE /api/v1/images/{imageId}
     */
    @DeleteMapping("/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable String imageId) {
        log.info("DELETE /api/v1/images/{}", imageId);
        
        imageService.delete(imageId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Request body for Base64 image upload.
     */
    public record ImageUploadRequest(
            String base64Data,
            String mimeType,
            String filename
    ) {}
}
