package com.catalogforge.model;

import java.util.Map;

/**
 * Technical specifications for a product.
 * Contains key-value pairs of specification data.
 */
public record TechnicalData(
    Map<String, String> specifications
) {
    public TechnicalData {
        specifications = specifications != null ? Map.copyOf(specifications) : Map.of();
    }
}
