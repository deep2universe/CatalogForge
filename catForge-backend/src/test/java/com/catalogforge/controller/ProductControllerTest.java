package com.catalogforge.controller;

import com.catalogforge.model.Product;
import com.catalogforge.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController.
 * Tests REST endpoints with mocked ProductService.
 */
@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    private Product createTestProduct(Long id, String name, String category, String series) {
        return new Product(
                id,
                name,
                "Short description",
                "Medium description",
                "Long description",
                category,
                series,
                null,
                List.of("Feature 1", "Feature 2"),
                "https://example.com/image.jpg",
                100000
        );
    }

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetProductsTests {

        @Test
        @DisplayName("should return all products")
        void shouldReturnAllProducts() throws Exception {
            List<Product> products = List.of(
                    createTestProduct(1L, "Actros L 1853", "Fernverkehr", "Actros L"),
                    createTestProduct(2L, "Arocs 3251", "Baustellenverkehr", "Arocs")
            );
            when(productService.getAllProducts()).thenReturn(products);

            mockMvc.perform(get("/api/v1/products"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", is("Actros L 1853")))
                    .andExpect(jsonPath("$[1].name", is("Arocs 3251")));
        }

        @Test
        @DisplayName("should filter by category")
        void shouldFilterByCategory() throws Exception {
            List<Product> filtered = List.of(
                    createTestProduct(1L, "Actros L 1853", "Fernverkehr", "Actros L")
            );
            when(productService.filterProducts("Fernverkehr", null)).thenReturn(filtered);

            mockMvc.perform(get("/api/v1/products")
                            .param("category", "Fernverkehr"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].category", is("Fernverkehr")));
        }

        @Test
        @DisplayName("should filter by series")
        void shouldFilterBySeries() throws Exception {
            List<Product> filtered = List.of(
                    createTestProduct(1L, "Actros L 1853", "Fernverkehr", "Actros L")
            );
            when(productService.filterProducts(null, "Actros L")).thenReturn(filtered);

            mockMvc.perform(get("/api/v1/products")
                            .param("series", "Actros L"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].series", is("Actros L")));
        }

        @Test
        @DisplayName("should filter by category and series")
        void shouldFilterByCategoryAndSeries() throws Exception {
            List<Product> filtered = List.of(
                    createTestProduct(1L, "Actros L 1853", "Fernverkehr", "Actros L")
            );
            when(productService.filterProducts("Fernverkehr", "Actros L")).thenReturn(filtered);

            mockMvc.perform(get("/api/v1/products")
                            .param("category", "Fernverkehr")
                            .param("series", "Actros L"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("should return product by ID")
        void shouldReturnProductById() throws Exception {
            Product product = createTestProduct(1L, "Actros L 1853", "Fernverkehr", "Actros L");
            when(productService.getProductById(1L)).thenReturn(product);

            mockMvc.perform(get("/api/v1/products/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.name", is("Actros L 1853")))
                    .andExpect(jsonPath("$.category", is("Fernverkehr")))
                    .andExpect(jsonPath("$.series", is("Actros L")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/categories")
    class GetCategoriesTests {

        @Test
        @DisplayName("should return all categories sorted")
        void shouldReturnAllCategories() throws Exception {
            Set<String> categories = Set.of("Baustellenverkehr", "Fernverkehr", "Verteilerverkehr");
            when(productService.getAllCategories()).thenReturn(categories);

            mockMvc.perform(get("/api/v1/products/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$", containsInAnyOrder("Baustellenverkehr", "Fernverkehr", "Verteilerverkehr")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/series")
    class GetSeriesTests {

        @Test
        @DisplayName("should return all series sorted")
        void shouldReturnAllSeries() throws Exception {
            Set<String> series = Set.of("Actros L", "Arocs", "Atego");
            when(productService.getAllSeries()).thenReturn(series);

            mockMvc.perform(get("/api/v1/products/series"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$", containsInAnyOrder("Actros L", "Arocs", "Atego")));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/search")
    class SearchProductsTests {

        @Test
        @DisplayName("should search products by query")
        void shouldSearchProducts() throws Exception {
            List<Product> results = List.of(
                    createTestProduct(1L, "Actros L 1853", "Fernverkehr", "Actros L"),
                    createTestProduct(2L, "Actros L 1848", "Fernverkehr", "Actros L")
            );
            when(productService.searchProducts("Actros")).thenReturn(results);

            mockMvc.perform(get("/api/v1/products/search")
                            .param("q", "Actros"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[0].name", containsString("Actros")));
        }

        @Test
        @DisplayName("should return empty list for no matches")
        void shouldReturnEmptyForNoMatches() throws Exception {
            when(productService.searchProducts("nonexistent")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/products/search")
                            .param("q", "nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
}
