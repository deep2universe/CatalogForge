package com.catalogforge.model.response;

import java.util.List;

import com.catalogforge.model.Product;

/**
 * Paginated response for product list requests.
 */
public record ProductListResponse(
    List<Product> products,
    int page,
    int size,
    long totalElements,
    int totalPages
) {
    public ProductListResponse {
        products = products != null ? List.copyOf(products) : List.of();
    }
}
