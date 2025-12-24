package com.catalogforge.controller;

import com.catalogforge.model.Layout;
import com.catalogforge.model.LayoutVariant;
import com.catalogforge.model.request.ImageToLayoutRequest;
import com.catalogforge.model.request.TextToLayoutRequest;
import com.catalogforge.model.response.LayoutResponse;
import com.catalogforge.service.LayoutGenerationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for layout generation and management.
 */
@RestController
@RequestMapping("/api/v1/layouts")
public class LayoutController {

    private static final Logger log = LoggerFactory.getLogger(LayoutController.class);

    private final LayoutGenerationService layoutService;

    public LayoutController(LayoutGenerationService layoutService) {
        this.layoutService = layoutService;
    }

    /**
     * Generates a layout from text prompt.
     * POST /api/v1/layouts/generate/text
     */
    @PostMapping("/generate/text")
    public ResponseEntity<LayoutResponse> generateFromText(@Valid @RequestBody TextToLayoutRequest request) {
        log.info("POST /api/v1/layouts/generate/text - products: {}", request.productIds());
        
        Layout layout = layoutService.generateFromText(
                request.productIds(),
                request.options(),
                request.prompt()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(LayoutResponse.from(layout));
    }

    /**
     * Generates a layout from image reference.
     * POST /api/v1/layouts/generate/image
     */
    @PostMapping("/generate/image")
    public ResponseEntity<LayoutResponse> generateFromImage(@Valid @RequestBody ImageToLayoutRequest request) {
        log.info("POST /api/v1/layouts/generate/image - products: {}, mimeType: {}", 
                request.productIds(), request.imageMimeType());
        
        Layout layout = layoutService.generateFromImage(
                request.productIds(),
                request.options(),
                request.prompt(),
                request.imageBase64(),
                request.imageMimeType()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(LayoutResponse.from(layout));
    }

    /**
     * Gets a layout by ID.
     * GET /api/v1/layouts/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<LayoutResponse> getLayout(@PathVariable String id) {
        log.debug("GET /api/v1/layouts/{}", id);
        
        Layout layout = layoutService.getLayout(id);
        return ResponseEntity.ok(LayoutResponse.from(layout));
    }

    /**
     * Updates a layout.
     * PUT /api/v1/layouts/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<LayoutResponse> updateLayout(
            @PathVariable String id,
            @RequestBody Layout updatedLayout
    ) {
        log.info("PUT /api/v1/layouts/{}", id);
        
        Layout layout = layoutService.updateLayout(id, updatedLayout);
        return ResponseEntity.ok(LayoutResponse.from(layout));
    }

    /**
     * Deletes a layout.
     * DELETE /api/v1/layouts/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLayout(@PathVariable String id) {
        log.info("DELETE /api/v1/layouts/{}", id);
        
        layoutService.deleteLayout(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Gets all variants for a layout.
     * GET /api/v1/layouts/{id}/variants
     */
    @GetMapping("/{id}/variants")
    public ResponseEntity<List<LayoutResponse.VariantResponse>> getVariants(@PathVariable String id) {
        log.debug("GET /api/v1/layouts/{}/variants", id);
        
        List<LayoutVariant> variants = layoutService.getVariants(id);
        List<LayoutResponse.VariantResponse> response = variants.stream()
                .map(LayoutResponse.VariantResponse::from)
                .toList();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Gets a specific variant.
     * GET /api/v1/layouts/{layoutId}/variants/{variantId}
     */
    @GetMapping("/{layoutId}/variants/{variantId}")
    public ResponseEntity<LayoutResponse.VariantResponse> getVariant(
            @PathVariable String layoutId,
            @PathVariable String variantId
    ) {
        log.debug("GET /api/v1/layouts/{}/variants/{}", layoutId, variantId);
        
        LayoutVariant variant = layoutService.getVariant(layoutId, variantId);
        return ResponseEntity.ok(LayoutResponse.VariantResponse.from(variant));
    }
}
