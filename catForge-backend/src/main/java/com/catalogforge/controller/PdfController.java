package com.catalogforge.controller;

import com.catalogforge.pdf.PrintPreset;
import com.catalogforge.service.PdfGenerationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * REST controller for PDF generation.
 */
@RestController
@RequestMapping("/api/v1/pdf")
public class PdfController {

    private static final Logger log = LoggerFactory.getLogger(PdfController.class);

    private final PdfGenerationService pdfService;

    public PdfController(PdfGenerationService pdfService) {
        this.pdfService = pdfService;
    }

    /**
     * Generates a PDF from a layout.
     * POST /api/v1/pdf/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generatePdf(@RequestBody PdfGenerateRequest request) {
        log.info("POST /api/v1/pdf/generate - layoutId: {}, preset: {}", 
                request.layoutId(), request.preset());

        String pdfId = pdfService.generate(
                request.layoutId(),
                request.variantId(),
                request.preset()
        );

        Map<String, Object> response = Map.of(
                "pdfId", pdfId,
                "downloadUrl", "/api/v1/pdf/" + pdfId + "/download"
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Downloads a generated PDF.
     * GET /api/v1/pdf/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id) {
        log.info("GET /api/v1/pdf/{}/download", id);

        PdfGenerationService.GeneratedPdf pdf = pdfService.getPdf(id);
        byte[] content = pdfService.getPdfContent(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("layout-" + pdf.layoutId() + ".pdf")
                .build());
        headers.setContentLength(content.length);

        return ResponseEntity.ok().headers(headers).body(content);
    }

    /**
     * Gets PDF metadata.
     * GET /api/v1/pdf/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPdfInfo(@PathVariable String id) {
        log.debug("GET /api/v1/pdf/{}", id);

        PdfGenerationService.GeneratedPdf pdf = pdfService.getPdf(id);

        Map<String, Object> response = Map.of(
                "id", pdf.id(),
                "layoutId", pdf.layoutId(),
                "variantId", pdf.variantId() != null ? pdf.variantId() : "",
                "preset", pdf.preset().getName(),
                "downloadUrl", "/api/v1/pdf/" + id + "/download"
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a generated PDF.
     * DELETE /api/v1/pdf/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePdf(@PathVariable String id) {
        log.info("DELETE /api/v1/pdf/{}", id);

        pdfService.deletePdf(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists available print presets.
     * GET /api/v1/pdf/presets
     */
    @GetMapping("/presets")
    public ResponseEntity<List<Map<String, Object>>> getPresets() {
        List<Map<String, Object>> presets = Arrays.stream(PrintPreset.values())
                .map(p -> Map.<String, Object>of(
                        "name", p.getName(),
                        "description", p.getDescription(),
                        "dpi", p.getDpi(),
                        "bleedMm", p.getBleedMm(),
                        "cropMarks", p.hasCropMarks()
                ))
                .toList();

        return ResponseEntity.ok(presets);
    }

    /**
     * Request body for PDF generation.
     */
    public record PdfGenerateRequest(
            String layoutId,
            String variantId,
            String preset
    ) {
        public PdfGenerateRequest {
            if (preset == null || preset.isBlank()) preset = "screen";
        }
    }
}
