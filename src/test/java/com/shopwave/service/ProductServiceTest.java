package com.shopwave.service;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.mapper.ProductMapper;
import com.shopwave.model.Product;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ProductServiceTest — unit tests for ProductService using Mockito.
 *
 * Key concepts:
 *   @ExtendWith(MockitoExtension.class) — enables Mockito annotations in JUnit 5
 *
 *   @Mock — creates a "fake" version of the dependency.
 *     We don't want the real DB or real ProductMapper in these tests —
 *     we want total control over what they return.
 *
 *   @InjectMocks — creates a real ProductService instance, but injects
 *     the @Mock objects as its dependencies (instead of real implementations).
 *
 * Why unit test with mocks?
 *   - FAST: no Spring context, no database needed
 *   - ISOLATED: we test ONLY the ProductService logic, nothing else
 *   - CONTROLLED: mocks return exactly what we tell them to
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    // These are FAKE/MOCK dependencies — not real implementations
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    // This is the REAL ProductService, with mocks injected
    @InjectMocks
    private ProductService productService;

    // Shared test data — set up fresh before each test
    private CreateProductRequest createRequest;
    private Product savedProduct;
    private ProductDTO expectedDTO;

    @BeforeEach
    void setUp() {
        // Build a request DTO that will be passed to createProduct()
        createRequest = CreateProductRequest.builder()
                .name("Test Product")
                .description("A product for testing")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .categoryId(null)  // no category for simplicity
                .build();

        // The entity that the repository would "save" and return
        savedProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("A product for testing")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .build();

        // The DTO we expect the service to return
        expectedDTO = ProductDTO.builder()
                .id(1L)
                .name("Test Product")
                .description("A product for testing")
                .price(new BigDecimal("99.99"))
                .stock(50)
                .build();
    }

    /**
     * HAPPY PATH TEST: createProduct() successfully creates and returns a product.
     *
     * We tell the mocks what to return (given), call the service (when),
     * then check the result (then). This is the "Given-When-Then" pattern.
     */
    @Test
    @DisplayName("createProduct - happy path - should save and return ProductDTO")
    void createProduct_happyPath_shouldReturnProductDTO() {
        // GIVEN: set up mock behaviour
        // When productMapper.toEntity() is called with any request and null category,
        // return our savedProduct (simulating the mapper doing its job).
        when(productMapper.toEntity(any(CreateProductRequest.class), isNull()))
                .thenReturn(savedProduct);

        // When productRepository.save() is called with any Product, return savedProduct.
        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        // When productMapper.toDTO() is called with savedProduct, return expectedDTO.
        when(productMapper.toDTO(savedProduct))
                .thenReturn(expectedDTO);

        // WHEN: call the real method we're testing
        ProductDTO result = productService.createProduct(createRequest);

        // THEN: assert the result is what we expect
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualByComparingTo("99.99");

        // Verify that save() was called exactly once — not zero, not twice
        verify(productRepository, times(1)).save(any(Product.class));

        // Verify that we never tried to look up a category (categoryId was null)
        verify(categoryRepository, never()).findById(any());
    }

    /**
     * ERROR PATH TEST: createProduct() throws when an invalid categoryId is given.
     *
     * We pass a categoryId that doesn't exist in the DB.
     * The mock categoryRepository returns empty Optional.
     * The service should throw IllegalArgumentException.
     */
    @Test
    @DisplayName("createProduct - category not found - should throw IllegalArgumentException")
    void createProduct_categoryNotFound_shouldThrowException() {
        // GIVEN: request has a categoryId
        createRequest.setCategoryId(99L);  // non-existent category

        // Mock: categoryRepository.findById(99) returns empty (category doesn't exist)
        when(categoryRepository.findById(99L))
                .thenReturn(Optional.empty());

        // WHEN + THEN: calling createProduct should throw IllegalArgumentException
        assertThatThrownBy(() -> productService.createProduct(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category not found with id: 99");

        // Verify that we tried to look up the category
        verify(categoryRepository, times(1)).findById(99L);

        // Verify that save() was NEVER called (no product should be saved on error)
        verify(productRepository, never()).save(any());
    }
}
