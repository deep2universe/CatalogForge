package com.catalogforge.service;

import com.catalogforge.exception.ResourceNotFoundException;
import com.catalogforge.model.Product;
import com.catalogforge.util.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service for managing product data.
 * Loads products from JSON file at startup and provides filtering, search, and lookup operations.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final String PRODUCTS_FILE = "data/products.json";

    private final Map<Long, Product> productsById = new ConcurrentHashMap<>();
    private final List<Product> allProducts = new ArrayList<>();
    private volatile Set<String> cachedCategories;
    private volatile Set<String> cachedSeries;

    @PostConstruct
    public void init() {
        loadProducts();
    }

    /**
     * Loads products from the JSON file into memory.
     */
    void loadProducts() {
        try {
            ClassPathResource resource = new ClassPathResource(PRODUCTS_FILE);
            try (InputStream is = resource.getInputStream()) {
                List<Product> products = JsonUtils.fromJson(is, new TypeReference<>() {});
                
                productsById.clear();
                allProducts.clear();
                
                for (Product product : products) {
                    productsById.put(product.id(), product);
                    allProducts.add(product);
                }
                
                // Invalidate caches
                cachedCategories = null;
                cachedSeries = null;
                
                log.info("Loaded {} products from {}", allProducts.size(), PRODUCTS_FILE);
            }
        } catch (IOException e) {
            log.error("Failed to load products from {}", PRODUCTS_FILE, e);
            throw new IllegalStateException("Could not load products", e);
        }
    }

    /**
     * Returns all products.
     */
    public List<Product> getAllProducts() {
        return Collections.unmodifiableList(allProducts);
    }

    /**
     * Returns a product by ID.
     * @throws ResourceNotFoundException if product not found
     */
    public Product getProductById(Long id) {
        Product product = productsById.get(id);
        if (product == null) {
            throw new ResourceNotFoundException("Product", String.valueOf(id));
        }
        return product;
    }

    /**
     * Returns an optional product by ID.
     */
    public Optional<Product> findProductById(Long id) {
        return Optional.ofNullable(productsById.get(id));
    }

    /**
     * Filters products by category and/or series.
     * Both filters are optional and case-insensitive.
     */
    public List<Product> filterProducts(String category, String series) {
        return allProducts.stream()
                .filter(p -> category == null || p.category().equalsIgnoreCase(category))
                .filter(p -> series == null || p.series().equalsIgnoreCase(series))
                .toList();
    }

    /**
     * Searches products by query string.
     * Performs case-insensitive full-text search across name, description, shortDescription, and series.
     */
    public List<Product> searchProducts(String query) {
        if (query == null || query.isBlank()) {
            return getAllProducts();
        }
        
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        
        return allProducts.stream()
                .filter(p -> matchesQuery(p, lowerQuery))
                .toList();
    }

    private boolean matchesQuery(Product product, String lowerQuery) {
        return containsIgnoreCase(product.name(), lowerQuery)
                || containsIgnoreCase(product.shortDescription(), lowerQuery)
                || containsIgnoreCase(product.description(), lowerQuery)
                || containsIgnoreCase(product.series(), lowerQuery)
                || containsIgnoreCase(product.category(), lowerQuery);
    }

    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase(Locale.ROOT).contains(query);
    }

    /**
     * Returns all unique categories, sorted alphabetically.
     */
    public Set<String> getAllCategories() {
        Set<String> categories = cachedCategories;
        if (categories == null) {
            categories = allProducts.stream()
                    .map(Product::category)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(TreeSet::new));
            cachedCategories = categories;
        }
        return Collections.unmodifiableSet(categories);
    }

    /**
     * Returns all unique series, sorted alphabetically.
     */
    public Set<String> getAllSeries() {
        Set<String> series = cachedSeries;
        if (series == null) {
            series = allProducts.stream()
                    .map(Product::series)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(TreeSet::new));
            cachedSeries = series;
        }
        return Collections.unmodifiableSet(series);
    }

    /**
     * Returns the total number of products.
     */
    public int getProductCount() {
        return allProducts.size();
    }
}
