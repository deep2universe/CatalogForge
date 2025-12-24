package com.catalogforge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;

/**
 * Technical specifications for a product.
 * Contains key-value pairs of specification data.
 * Serializes/deserializes directly as a Map for cleaner JSON structure.
 */
public record TechnicalData(
    Map<String, String> specifications
) {
    public TechnicalData {
        specifications = specifications != null ? Map.copyOf(specifications) : Map.of();
    }

    /**
     * Creates TechnicalData directly from a Map (for JSON deserialization).
     */
    @JsonCreator
    public static TechnicalData fromMap(Map<String, String> specs) {
        return new TechnicalData(specs);
    }

    /**
     * Returns the specifications map directly (for JSON serialization).
     */
    @JsonValue
    public Map<String, String> specifications() {
        return specifications;
    }
}
