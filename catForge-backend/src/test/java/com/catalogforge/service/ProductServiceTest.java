package com.catalogforge.service;

import com.catalogforge.exception.ResourceNotFoundException;
import com.catalogforge.model.Product;
import net.jqwik.api.*;
import net.jqwik.api.constraints.NotBlank;
import net.jqwik.api.constraints.Size;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for ProductService including property-based tests.
 * 
 * Property Tests validate:
 * - Property 1: Product Data Integrity (Requirements 1.1, 1.3)
 * - Property 2: Product Filtering Correctness (Requirements 1.2)
 * - Property 3: Search Result Relevance (Requirements 1.5)
 * - Property 4: Categories and Series Uniqueness (Requirements 1.6, 1.7)
 */
class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService();
        productService.init();
    }

    @Nested
    @DisplayName("Product Loading Tests")
    class ProductLoadingTests {

        @Test
        @DisplayName("should load all 30 products from JSON file")
        void shouldLoadAllProducts() {
            assertThat(productService.getProductCount()).isEqualTo(30);
        }

        @Test
        @DisplayName("should have products with valid IDs")
        void shouldHaveValidIds() {
            List<Product> products = productService.getAllProducts();
            assertThat(products)
                    .extracting(Product::id)
                    .doesNotContainNull()
                    .doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("should have products with required fields")
        void shouldHaveRequiredFields() {
            List<Product> products = productService.getAllProducts();
            assertThat(products).allSatisfy(product -> {
                assertThat(product.id()).isNotNull();
                assertThat(product.name()).isNotBlank();
                assertThat(product.category()).isNotBlank();
                assertThat(product.series()).isNotBlank();
                assertThat(product.shortDescription()).isNotBlank();
                assertThat(product.description()).isNotBlank();
            });
        }
    }

    @Nested
    @DisplayName("Product Lookup Tests")
    class ProductLookupTests {

        @Test
        @DisplayName("should find product by valid ID")
        void shouldFindProductById() {
            Product product = productService.getProductById(1L);
            assertThat(product).isNotNull();
            assertThat(product.id()).isEqualTo(1L);
            assertThat(product.name()).contains("Actros");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException for invalid ID")
        void shouldThrowForInvalidId() {
            assertThatThrownBy(() -> productService.getProductById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Product")
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("should return empty optional for non-existent ID")
        void shouldReturnEmptyOptional() {
            assertThat(productService.findProductById(999L)).isEmpty();
        }

        @Test
        @DisplayName("should return present optional for existing ID")
        void shouldReturnPresentOptional() {
            assertThat(productService.findProductById(1L)).isPresent();
        }
    }

    @Nested
    @DisplayName("Property 1: Product Data Integrity")
    class ProductDataIntegrityPropertyTests {

        @Property
        @Label("All product IDs should be unique")
        void allProductIdsShouldBeUnique() {
            List<Product> products = productService.getAllProducts();
            Set<Long> ids = Set.copyOf(products.stream().map(Product::id).toList());
            assertThat(ids).hasSize(products.size());
        }

        @Property
        @Label("All products should have non-empty names")
        void allProductsShouldHaveNames() {
            List<Product> products = productService.getAllProducts();
            assertThat(products)
                    .extracting(Product::name)
                    .allMatch(name -> name != null && !name.isBlank());
        }

        @Property
        @Label("All products should have valid categories")
        void allProductsShouldHaveCategories() {
            List<Product> products = productService.getAllProducts();
            Set<String> validCategories = productService.getAllCategories();
            
            assertThat(products)
                    .extracting(Product::category)
                    .allMatch(validCategories::contains);
        }

        @Property
        @Label("All products should have valid series")
        void allProductsShouldHaveSeries() {
            List<Product> products = productService.getAllProducts();
            Set<String> validSeries = productService.getAllSeries();
            
            assertThat(products)
                    .extracting(Product::series)
                    .allMatch(validSeries::contains);
        }
    }

    @Nested
    @DisplayName("Property 2: Product Filtering Correctness")
    class ProductFilteringPropertyTests {

        @Property
        @Label("Filtering by category should return only products with that category")
        void filteringByCategoryShouldBeCorrect() {
            Set<String> categories = productService.getAllCategories();
            
            for (String category : categories) {
                List<Product> filtered = productService.filterProducts(category, null);
                assertThat(filtered)
                        .isNotEmpty()
                        .allMatch(p -> p.category().equalsIgnoreCase(category));
            }
        }

        @Property
        @Label("Filtering by series should return only products with that series")
        void filteringBySeriesShouldBeCorrect() {
            Set<String> seriesList = productService.getAllSeries();
            
            for (String series : seriesList) {
                List<Product> filtered = productService.filterProducts(null, series);
                assertThat(filtered)
                        .isNotEmpty()
                        .allMatch(p -> p.series().equalsIgnoreCase(series));
            }
        }

        @Property
        @Label("Filtering with null parameters should return all products")
        void filteringWithNullShouldReturnAll() {
            List<Product> filtered = productService.filterProducts(null, null);
            assertThat(filtered).hasSize(productService.getProductCount());
        }

        @Property
        @Label("Combined filtering should be intersection of both filters")
        void combinedFilteringShouldBeIntersection() {
            // Get a category and series that exist together
            Product firstProduct = productService.getAllProducts().get(0);
            String category = firstProduct.category();
            String series = firstProduct.series();
            
            List<Product> filtered = productService.filterProducts(category, series);
            
            assertThat(filtered)
                    .allMatch(p -> p.category().equalsIgnoreCase(category))
                    .allMatch(p -> p.series().equalsIgnoreCase(series));
        }
    }

    @Nested
    @DisplayName("Property 3: Search Result Relevance")
    class SearchRelevancePropertyTests {

        @Property
        @Label("Search results should contain query in searchable fields")
        void searchResultsShouldContainQuery() {
            // Search for "Actros" - should find multiple products
            List<Product> results = productService.searchProducts("Actros");
            
            assertThat(results)
                    .isNotEmpty()
                    .allMatch(p -> 
                            containsIgnoreCase(p.name(), "Actros") ||
                            containsIgnoreCase(p.shortDescription(), "Actros") ||
                            containsIgnoreCase(p.description(), "Actros") ||
                            containsIgnoreCase(p.series(), "Actros") ||
                            containsIgnoreCase(p.category(), "Actros")
                    );
        }

        @Property
        @Label("Search should be case-insensitive")
        void searchShouldBeCaseInsensitive() {
            List<Product> upperResults = productService.searchProducts("ACTROS");
            List<Product> lowerResults = productService.searchProducts("actros");
            List<Product> mixedResults = productService.searchProducts("AcTrOs");
            
            assertThat(upperResults).hasSameElementsAs(lowerResults);
            assertThat(lowerResults).hasSameElementsAs(mixedResults);
        }

        @Property
        @Label("Empty search should return all products")
        void emptySearchShouldReturnAll() {
            assertThat(productService.searchProducts("")).hasSize(productService.getProductCount());
            assertThat(productService.searchProducts(null)).hasSize(productService.getProductCount());
            assertThat(productService.searchProducts("   ")).hasSize(productService.getProductCount());
        }

        @Property
        @Label("Search for non-existent term should return empty list")
        void searchForNonExistentShouldReturnEmpty() {
            List<Product> results = productService.searchProducts("xyznonexistent123");
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Search should find products by series name")
        void searchShouldFindBySeries() {
            List<Product> results = productService.searchProducts("Unimog");
            assertThat(results)
                    .isNotEmpty()
                    .allMatch(p -> p.series().contains("Unimog") || 
                                   p.name().contains("Unimog") ||
                                   p.description().contains("Unimog"));
        }

        @Test
        @DisplayName("Search should find products by category")
        void searchShouldFindByCategory() {
            List<Product> results = productService.searchProducts("Elektro");
            assertThat(results).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Property 4: Categories and Series Uniqueness")
    class CategoriesSeriesUniquenessPropertyTests {

        @Property
        @Label("Categories should be unique and sorted")
        void categoriesShouldBeUniqueAndSorted() {
            Set<String> categories = productService.getAllCategories();
            
            assertThat(categories)
                    .isNotEmpty()
                    .doesNotContainNull()
                    .allMatch(c -> !c.isBlank());
            
            // Verify sorted
            List<String> categoryList = List.copyOf(categories);
            assertThat(categoryList).isSorted();
        }

        @Property
        @Label("Series should be unique and sorted")
        void seriesShouldBeUniqueAndSorted() {
            Set<String> series = productService.getAllSeries();
            
            assertThat(series)
                    .isNotEmpty()
                    .doesNotContainNull()
                    .allMatch(s -> !s.isBlank());
            
            // Verify sorted
            List<String> seriesList = List.copyOf(series);
            assertThat(seriesList).isSorted();
        }

        @Property
        @Label("Every product category should be in getAllCategories")
        void everyProductCategoryShouldBeInGetAllCategories() {
            Set<String> categories = productService.getAllCategories();
            List<Product> products = productService.getAllProducts();
            
            for (Product product : products) {
                assertThat(categories).contains(product.category());
            }
        }

        @Property
        @Label("Every product series should be in getAllSeries")
        void everyProductSeriesShouldBeInGetAllSeries() {
            Set<String> series = productService.getAllSeries();
            List<Product> products = productService.getAllProducts();
            
            for (Product product : products) {
                assertThat(series).contains(product.series());
            }
        }
    }

    @Nested
    @DisplayName("Specific Product Data Tests")
    class SpecificProductDataTests {

        @Test
        @DisplayName("should have Actros L 1853 LS ProCabin as first product")
        void shouldHaveActrosAsFirstProduct() {
            Product product = productService.getProductById(1L);
            assertThat(product.name()).isEqualTo("Mercedes-Benz Actros L 1853 LS ProCabin");
            assertThat(product.series()).isEqualTo("Actros L");
            assertThat(product.category()).isEqualTo("Fernverkehr");
            assertThat(product.priceEur()).isEqualTo(189500);
        }

        @Test
        @DisplayName("should have eActros 600 as electric truck")
        void shouldHaveEActros600() {
            Product product = productService.getProductById(6L);
            assertThat(product.name()).contains("eActros 600");
            assertThat(product.category()).contains("Elektro");
            assertThat(product.specs().specifications()).containsKey("batteriekapazitaet_kwh");
        }

        @Test
        @DisplayName("should have highlights for products")
        void shouldHaveHighlights() {
            Product product = productService.getProductById(1L);
            assertThat(product.highlights())
                    .isNotEmpty()
                    .contains("Active Brake Assist 6");
        }

        @Test
        @DisplayName("should have technical specs")
        void shouldHaveTechnicalSpecs() {
            Product product = productService.getProductById(1L);
            assertThat(product.specs().specifications())
                    .containsKey("motor")
                    .containsKey("leistung_ps")
                    .containsKey("gesamtgewicht_t");
        }
    }

    private boolean containsIgnoreCase(String text, String query) {
        return text != null && text.toLowerCase().contains(query.toLowerCase());
    }
}
