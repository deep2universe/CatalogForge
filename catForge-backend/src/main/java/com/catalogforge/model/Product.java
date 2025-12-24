package com.catalogforge.model;

/**
 * Represents a product in the catalog.
 * Products are loaded from products.json and used for layout generation.
 */
public record Product(
    Long id,
    String name,
    String description,
    String category,
    String series,
    TechnicalData specs,
    String imageUrl
) {}
