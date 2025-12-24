package com.catalogforge.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.io.InputStream;

/**
 * Utility for JSON serialization and deserialization.
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = createObjectMapper();

    private JsonUtils() {
        // Utility class
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    /**
     * Get the shared ObjectMapper instance.
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * Serialize object to JSON string.
     */
    public static String toJson(Object obj) {
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    /**
     * Serialize object to pretty-printed JSON string.
     */
    public static String toPrettyJson(Object obj) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize to JSON", e);
        }
    }

    /**
     * Deserialize JSON string to object.
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize from JSON", e);
        }
    }

    /**
     * Deserialize JSON from InputStream using TypeReference for generic types.
     */
    public static <T> T fromJson(InputStream is, TypeReference<T> typeRef) throws IOException {
        return MAPPER.readValue(is, typeRef);
    }

    /**
     * Deserialize JSON string using TypeReference for generic types.
     */
    public static <T> T fromJson(String json, TypeReference<T> typeRef) {
        try {
            return MAPPER.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize from JSON", e);
        }
    }

    /**
     * Convert object to another type via JSON.
     */
    public static <T> T convert(Object obj, Class<T> clazz) {
        return MAPPER.convertValue(obj, clazz);
    }
}
