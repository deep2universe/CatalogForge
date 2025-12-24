package com.catalogforge.pdf;

/**
 * Options for PDF generation.
 */
public record PdfOptions(
    PrintPreset preset,
    String pageFormat,
    boolean landscape,
    String headerHtml,
    String footerHtml
) {
    public PdfOptions {
        if (preset == null) preset = PrintPreset.SCREEN;
        if (pageFormat == null || pageFormat.isBlank()) pageFormat = "A4";
    }

    public static PdfOptions defaults() {
        return new PdfOptions(PrintPreset.SCREEN, "A4", false, null, null);
    }

    public static PdfOptions forPrint() {
        return new PdfOptions(PrintPreset.PRINT_STANDARD, "A4", false, null, null);
    }

    public static PdfOptions forProfessionalPrint() {
        return new PdfOptions(PrintPreset.PRINT_PROFESSIONAL, "A4", false, null, null);
    }
}
