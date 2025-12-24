package com.catalogforge.controller;

import com.catalogforge.model.Product;
import com.catalogforge.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * REST controller for product operations.
 * Provides endpoints for listing, filtering, searching, and retrieving products.
 */
@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * GET /api/v1/products
     * Returns all products, optionally filtered by category and/or series.
     */
    @GetMapping
    public ResponseEntity<List<Product>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String series) {
        
        List<Product> products;
        if (category != null || series != null) {
            products = productService.filterProducts(category, series);
        } else {
            products = productService.getAllProducts();
        }
        return ResponseEntity.ok(products);
    }

    /**
     * GET /api/v1/products/{id}
     * Returns a single product by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * GET /api/v1/products/categories
     * Returns all unique product categories, sorted alphabetically.
     */
    @GetMapping("/categories")
    public ResponseEntity<Set<String>> getCategories() {
        return ResponseEntity.ok(productService.getAllCategories());
    }

    /**
     * GET /api/v1/products/series
     * Returns all unique product series, sorted alphabetically.
     */
    @GetMapping("/series")
    public ResponseEntity<Set<String>> getSeries() {
        return ResponseEntity.ok(productService.getAllSeries());
    }

    /**
     * GET /api/v1/products/search?q={query}
     * Searches products by query string (case-insensitive full-text search).
     */
    @GetMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String q) {
        List<Product> results = productService.searchProducts(q);
        return ResponseEntity.ok(results);
    }
}
