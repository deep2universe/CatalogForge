package com.catalogforge.model.response;

/**
 * Response for PDF generation requests.
 */
public record PdfResponse(
    String pdfId,
    String downloadUrl
) {}
