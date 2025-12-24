package com.catalogforge.model;

import java.util.List;

/**
 * Represents a product in the catalog.
 * Products are loaded from products.json and used for layout generation.
 * 
 * Contains multiple text variants for different layout needs:
 * - shortDescription: 1-2 sentences for compact layouts
 * - description: ~150 words for standard layouts  
 * - longDescription: 300+ words for detailed product pages
 * - highlights: Feature list for bullet-point displays
 */
public record Product(
    Long id,
    String name,
    String shortDescription,
    String description,
    String longDescription,
    String category,
    String series,
    TechnicalData specs,
    List<String> highlights,
    String imageUrl,
    Integer priceEur
) {
    public Product {
        highlights = highlights != null ? List.copyOf(highlights) : List.of();
    }
}
