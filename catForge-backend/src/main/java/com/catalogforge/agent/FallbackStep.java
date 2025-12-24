package com.catalogforge.agent;

import com.catalogforge.model.Layout;
import com.catalogforge.model.LayoutMetadata;
import com.catalogforge.model.LayoutVariant;
import com.catalogforge.model.PageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Provides a fallback layout when generation repeatedly fails.
 * Creates a minimal but valid layout structure.
 */
@Component
public class FallbackStep implements AgentStep {

    private static final Logger log = LoggerFactory.getLogger(FallbackStep.class);

    @Override
    public AgentContext execute(AgentContext context) {
        log.warn("Executing fallback for pipeline: {} after {} retries", 
                context.pipelineId(), context.retryCount());
        
        Layout fallbackLayout = createFallbackLayout(context);
        
        return context
                .withGeneratedLayout(fallbackLayout)
                .withValidationErrors(List.of()); // Clear errors
    }

    private Layout createFallbackLayout(AgentContext context) {
        String productInfo = buildProductInfo(context);
        
        String html = String.format("""
                <!DOCTYPE html>
                <html lang="de">
                <head>
                    <meta charset="UTF-8">
                    <title>Produktkatalog</title>
                </head>
                <body>
                    <div class="catalog-page">
                        <header class="page-header">
                            <h1>Produktkatalog</h1>
                        </header>
                        <main class="content">
                            %s
                        </main>
                        <footer class="page-footer">
                            <p>Generiert mit CatalogForge</p>
                        </footer>
                    </div>
                </body>
                </html>
                """, productInfo);
        
        String css = """
                * {
                    margin: 0;
                    padding: 0;
                    box-sizing: border-box;
                }
                
                body {
                    font-family: 'Segoe UI', system-ui, sans-serif;
                    line-height: 1.6;
                    color: #333;
                    background: #fff;
                }
                
                .catalog-page {
                    max-width: 210mm;
                    margin: 0 auto;
                    padding: 20mm;
                }
                
                .page-header {
                    text-align: center;
                    margin-bottom: 2rem;
                    padding-bottom: 1rem;
                    border-bottom: 2px solid #0066cc;
                }
                
                .page-header h1 {
                    font-size: 2rem;
                    color: #0066cc;
                }
                
                .content {
                    min-height: 200mm;
                }
                
                .product-card {
                    background: #f8f9fa;
                    border-radius: 8px;
                    padding: 1.5rem;
                    margin-bottom: 1.5rem;
                }
                
                .product-card h2 {
                    color: #333;
                    margin-bottom: 0.5rem;
                }
                
                .product-card p {
                    color: #666;
                }
                
                .page-footer {
                    margin-top: 2rem;
                    padding-top: 1rem;
                    border-top: 1px solid #ddd;
                    text-align: center;
                    font-size: 0.875rem;
                    color: #666;
                }
                
                @media print {
                    .catalog-page {
                        padding: 15mm;
                    }
                }
                """;
        
        LayoutVariant variant = new LayoutVariant(
                UUID.randomUUID().toString(),
                html,
                css
        );
        
        String format = context.options() != null ? context.options().pageFormat() : "A4";
        PageFormat pageFormat = new PageFormat(format, 210, 297, "mm");
        
        return new Layout(
                UUID.randomUUID().toString(),
                "fallback",
                Instant.now(),
                pageFormat,
                context.imageAnalysis(),
                List.of(variant),
                new LayoutMetadata(List.of(), 0L, context.retryCount() + 1)
        );
    }

    private String buildProductInfo(AgentContext context) {
        if (context.products() == null || context.products().isEmpty()) {
            return "<p>Keine Produkte ausgewählt.</p>";
        }
        
        StringBuilder sb = new StringBuilder();
        for (var product : context.products()) {
            sb.append("<div class=\"product-card\">\n");
            sb.append("  <h2>").append(escapeHtml(product.name())).append("</h2>\n");
            if (product.shortDescription() != null) {
                sb.append("  <p>").append(escapeHtml(product.shortDescription())).append("</p>\n");
            }
            sb.append("</div>\n");
        }
        return sb.toString();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    @Override
    public String name() {
        return "Fallback";
    }
}
