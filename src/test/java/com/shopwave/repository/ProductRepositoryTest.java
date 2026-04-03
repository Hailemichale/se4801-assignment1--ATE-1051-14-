package com.shopwave.repository;

import com.shopwave.model.Category;
import com.shopwave.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ProductRepositoryTest — integration tests for the JPA repository layer.
 *
 * @DataJpaTest:
 *   - Loads ONLY the JPA-related components (entities, repositories, EntityManager)
 *   - Replaces the configured datasource with an H2 in-memory database automatically
 *   - Wraps each test in a transaction that is ROLLED BACK after the test
 *     → so test data doesn't persist between tests (good isolation!)
 *   - Much lighter than @SpringBootTest
 *
 * TestEntityManager:
 *   - A test-friendly wrapper around EntityManager
 *   - Used to insert test data directly into the DB before each test
 *   - Better than calling the repository itself for setup (no chicken-and-egg problem)
 *
 * We're testing the derived query methods defined in ProductRepository.
 */
@DataJpaTest
class ProductRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;  // for inserting test data

    @Autowired
    private ProductRepository productRepository;

    // Test data inserted before each test
    private Category electronics;
    private Category clothing;

    @BeforeEach
    void setUp() {
        // Create and persist categories first (products have FK to categories)
        electronics = entityManager.persist(Category.builder()
                .name("Electronics")
                .description("Gadgets and devices")
                .build());

        clothing = entityManager.persist(Category.builder()
                .name("Clothing")
                .description("Shirts, trousers, etc.")
                .build());

        // Persist some products
        entityManager.persist(Product.builder()
                .name("iPhone 15 Pro")
                .description("Apple flagship phone")
                .price(new BigDecimal("999.99"))
                .stock(20)
                .category(electronics)
                .build());

        entityManager.persist(Product.builder()
                .name("Samsung Galaxy Phone")
                .description("Android flagship phone")
                .price(new BigDecimal("799.99"))
                .stock(15)
                .category(electronics)
                .build());

        entityManager.persist(Product.builder()
                .name("Nike T-Shirt")
                .description("Cotton t-shirt")
                .price(new BigDecimal("29.99"))
                .stock(100)
                .category(clothing)
                .build());

        entityManager.persist(Product.builder()
                .name("Laptop Pro 16")
                .description("High-performance laptop")
                .price(new BigDecimal("1999.99"))
                .stock(5)
                .category(electronics)
                .build());

        // Flush makes sure all the above inserts hit the DB before the test runs
        entityManager.flush();
    }

    /**
     * Test findByNameContainingIgnoreCase.
     *
     * We search for "phone" — should match "iPhone 15 Pro" and "Samsung Galaxy S24"
     * but NOT "Nike T-Shirt" or "Laptop Pro 16".
     *
     * Also tests case-insensitivity: "PHONE" should match "phone" in names.
     */
    @Test
    @DisplayName("findByNameContainingIgnoreCase - should return products matching keyword case-insensitively")
    void findByNameContainingIgnoreCase_shouldReturnMatchingProducts() {
        // WHEN: search with lowercase
        List<Product> results = productRepository.findByNameContainingIgnoreCase("phone");

        // THEN: should find "iPhone 15 Pro" and "Samsung Galaxy S24"
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("iPhone 15 Pro", "Samsung Galaxy Phone");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase - uppercase keyword should still match")
    void findByNameContainingIgnoreCase_uppercaseKeyword_shouldStillMatch() {
        // WHEN: search with uppercase — should still match (IgnoreCase!)
        List<Product> results = productRepository.findByNameContainingIgnoreCase("IPHONE");

        // THEN
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("iPhone 15 Pro");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase - no match should return empty list")
    void findByNameContainingIgnoreCase_noMatch_shouldReturnEmpty() {
        // WHEN: search for something that doesn't exist
        List<Product> results = productRepository.findByNameContainingIgnoreCase("Blender");

        // THEN: empty list, not null
        assertThat(results).isEmpty();
    }

    /**
     * Quick sanity check: findByCategoryId returns only products for that category.
     */
    @Test
    @DisplayName("findByCategoryId - should only return products in that category")
    void findByCategoryId_shouldReturnProductsForCategory() {
        List<Product> electronicsProducts = productRepository.findByCategoryId(electronics.getId());

        // 3 electronics products: iPhone, Samsung, Laptop
        assertThat(electronicsProducts).hasSize(3);

        List<Product> clothingProducts = productRepository.findByCategoryId(clothing.getId());
        // 1 clothing product: Nike T-Shirt
        assertThat(clothingProducts).hasSize(1);
        assertThat(clothingProducts.get(0).getName()).isEqualTo("Nike T-Shirt");
    }
}
