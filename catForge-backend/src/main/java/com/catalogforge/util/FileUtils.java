package com.catalogforge.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Utility for file operations.
 */
public final class FileUtils {

    private FileUtils() {
        // Utility class
    }

    /**
     * Read file content as string from classpath.
     *
     * @param resourcePath path relative to classpath (e.g., "data/products.json")
     * @return file content as string
     * @throws IOException if file cannot be read
     */
    public static String readClasspathResource(String resourcePath) throws IOException {
        Resource resource = new ClassPathResource(resourcePath);
        try (InputStream is = resource.getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Read file content as string from filesystem.
     *
     * @param path file path
     * @return file content as string
     * @throws IOException if file cannot be read
     */
    public static String readFile(Path path) throws IOException {
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    /**
     * Write string content to file.
     *
     * @param path file path
     * @param content content to write
     * @throws IOException if file cannot be written
     */
    public static void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8);
    }

    /**
     * Append string content to file.
     *
     * @param path file path
     * @param content content to append
     * @throws IOException if file cannot be written
     */
    public static void appendToFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardCharsets.UTF_8, 
            java.nio.file.StandardOpenOption.CREATE,
            java.nio.file.StandardOpenOption.APPEND);
    }

    /**
     * Check if a classpath resource exists.
     */
    public static boolean classpathResourceExists(String resourcePath) {
        return new ClassPathResource(resourcePath).exists();
    }

    /**
     * Get file extension from filename.
     */
    public static String getExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
