package com.catalogforge.service;

import com.catalogforge.exception.PdfGenerationException;
import com.catalogforge.exception.ResourceNotFoundException;
import com.catalogforge.model.Layout;
import com.catalogforge.model.LayoutVariant;
import com.catalogforge.pdf.PdfOptions;
import com.catalogforge.pdf.PrintPreset;
import com.catalogforge.pdf.PuppeteerBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for PDF generation from layouts.
 */
@Service
public class PdfGenerationService {

    private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);

    private final LayoutGenerationService layoutService;
    private final PuppeteerBridge puppeteerBridge;
    private final Map<String, GeneratedPdf> pdfStore = new ConcurrentHashMap<>();

    public PdfGenerationService(LayoutGenerationService layoutService, PuppeteerBridge puppeteerBridge) {
        this.layoutService = layoutService;
        this.puppeteerBridge = puppeteerBridge;
    }

    /**
     * Generates a PDF from a layout.
     * 
     * @param layoutId The layout ID
     * @param variantId Optional variant ID (uses first variant if null)
     * @param presetName Print preset name
     * @return The generated PDF ID
     */
    public String generate(String layoutId, String variantId, String presetName) {
        log.info("Generating PDF: layoutId={}, variantId={}, preset={}", 
                layoutId, variantId, presetName);

        Layout layout = layoutService.getLayout(layoutId);
        LayoutVariant variant = resolveVariant(layout, variantId);
        PrintPreset preset = PrintPreset.fromName(presetName);

        String html = buildFullHtml(variant, layout);
        String css = variant.css();

        PdfOptions options = new PdfOptions(
                preset,
                layout.pageFormat() != null ? layout.pageFormat().name() : "A4",
                false,
                null,
                null
        );

        Path pdfPath = puppeteerBridge.generatePdf(html, css, options);
        
        String pdfId = UUID.randomUUID().toString();
        GeneratedPdf pdf = new GeneratedPdf(
                pdfId,
                layoutId,
                variantId,
                preset,
                pdfPath
        );
        
        pdfStore.put(pdfId, pdf);
        log.info("PDF generated: pdfId={}, path={}", pdfId, pdfPath);
        
        return pdfId;
    }

    /**
     * Gets a generated PDF by ID.
     */
    public GeneratedPdf getPdf(String pdfId) {
        return Optional.ofNullable(pdfStore.get(pdfId))
                .orElseThrow(() -> new ResourceNotFoundException("PDF", pdfId));
    }

    /**
     * Gets the PDF file content.
     */
    public byte[] getPdfContent(String pdfId) {
        GeneratedPdf pdf = getPdf(pdfId);
        try {
            return Files.readAllBytes(pdf.path());
        } catch (IOException e) {
            throw new PdfGenerationException("Failed to read PDF file", e);
        }
    }

    /**
     * Deletes a generated PDF.
     */
    public void deletePdf(String pdfId) {
        GeneratedPdf pdf = pdfStore.remove(pdfId);
        if (pdf == null) {
            throw new ResourceNotFoundException("PDF", pdfId);
        }
        
        try {
            Files.deleteIfExists(pdf.path());
            log.info("PDF deleted: pdfId={}", pdfId);
        } catch (IOException e) {
            log.warn("Failed to delete PDF file: {}", pdf.path(), e);
        }
    }

    private LayoutVariant resolveVariant(Layout layout, String variantId) {
        if (layout.variants() == null || layout.variants().isEmpty()) {
            throw new PdfGenerationException("Layout has no variants");
        }

        if (variantId == null || variantId.isBlank()) {
            return layout.variants().get(0);
        }

        return layout.variants().stream()
                .filter(v -> v.id().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Variant", variantId));
    }

    /**
     * Returns the HTML body content for PDF generation.
     * Note: The full HTML document wrapping is handled by pdf-generator.js
     * to avoid double-wrapping issues.
     */
    private String buildFullHtml(LayoutVariant variant, Layout layout) {
        // Return only the body content - pdf-generator.js handles the full document structure
        return variant.html();
    }

    /**
     * Record for stored PDF metadata.
     */
    public record GeneratedPdf(
            String id,
            String layoutId,
            String variantId,
            PrintPreset preset,
            Path path
    ) {}
}
