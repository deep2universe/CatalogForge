package com.catalogforge.pdf;

import com.catalogforge.exception.PdfGenerationException;
import com.catalogforge.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Bridge to Puppeteer/Node.js for PDF generation.
 * Communicates via stdin/stdout JSON.
 */
@Component
public class PuppeteerBridge {

    private static final Logger log = LoggerFactory.getLogger(PuppeteerBridge.class);
    private static final int TIMEOUT_SECONDS = 60;

    private final String nodeCommand;
    private final String scriptPath;
    private final boolean enabled;

    public PuppeteerBridge(
            @Value("${catalogforge.pdf.node-command:node}") String nodeCommand,
            @Value("${catalogforge.pdf.script-path:scripts/pdf-generator.js}") String scriptPath,
            @Value("${catalogforge.pdf.enabled:false}") boolean enabled
    ) {
        this.nodeCommand = nodeCommand;
        this.scriptPath = scriptPath;
        this.enabled = enabled;
    }

    /**
     * Generates a PDF from HTML content.
     * 
     * @param html The HTML content
     * @param css The CSS styles
     * @param options PDF generation options
     * @return Path to the generated PDF file
     */
    public Path generatePdf(String html, String css, PdfOptions options) {
        if (!enabled) {
            log.warn("PDF generation is disabled, returning placeholder");
            return createPlaceholderPdf(options);
        }

        log.debug("Generating PDF: preset={}, format={}", 
                options.preset().getName(), options.pageFormat());

        try {
            // Prepare input JSON
            Map<String, Object> input = Map.of(
                    "html", html,
                    "css", css,
                    "preset", options.preset().getName(),
                    "pageFormat", options.pageFormat(),
                    "landscape", options.landscape(),
                    "dpi", options.preset().getDpi(),
                    "bleedMm", options.preset().getBleedMm(),
                    "cropMarks", options.preset().hasCropMarks()
            );
            String inputJson = JsonUtils.toJson(input);

            // Execute Node.js script
            ProcessBuilder pb = new ProcessBuilder(nodeCommand, scriptPath);
            pb.redirectErrorStream(false);
            
            Process process = pb.start();

            // Write input to stdin
            try (OutputStream stdin = process.getOutputStream()) {
                stdin.write(inputJson.getBytes());
                stdin.flush();
            }

            // Read output from stdout
            String output;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                output = reader.lines().reduce("", (a, b) -> a + b);
            }

            // Read errors
            String errors;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()))) {
                errors = reader.lines().reduce("", (a, b) -> a + "\n" + b);
            }

            boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!completed) {
                process.destroyForcibly();
                throw new PdfGenerationException("PDF generation timed out");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("PDF generation failed: exitCode={}, errors={}", exitCode, errors);
                throw new PdfGenerationException("PDF generation failed: " + errors);
            }

            // Parse output
            Map<String, Object> result = JsonUtils.fromJson(output, new TypeReference<>() {});
            String pdfPath = (String) result.get("pdfPath");
            
            if (pdfPath == null) {
                throw new PdfGenerationException("No PDF path in response");
            }

            log.info("PDF generated: {}", pdfPath);
            return Path.of(pdfPath);

        } catch (IOException | InterruptedException e) {
            log.error("PDF generation error", e);
            throw new PdfGenerationException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Creates a placeholder PDF when generation is disabled.
     */
    private Path createPlaceholderPdf(PdfOptions options) {
        try {
            Path tempFile = Files.createTempFile("placeholder-", ".pdf");
            // Write minimal PDF content
            String minimalPdf = "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                    "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n" +
                    "3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R>>endobj\n" +
                    "xref\n0 4\n0000000000 65535 f\n0000000009 00000 n\n" +
                    "0000000052 00000 n\n0000000101 00000 n\n" +
                    "trailer<</Size 4/Root 1 0 R>>\nstartxref\n178\n%%EOF";
            Files.writeString(tempFile, minimalPdf);
            return tempFile;
        } catch (IOException e) {
            throw new PdfGenerationException("Failed to create placeholder PDF", e);
        }
    }

    /**
     * Checks if PDF generation is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
