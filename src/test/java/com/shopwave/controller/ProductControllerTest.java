package com.shopwave.controller;

import com.shopwave.dto.ProductDTO;
import com.shopwave.exception.GlobalExceptionHandler;
import com.shopwave.exception.ProductNotFoundException;
import com.shopwave.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProductControllerTest — tests the HTTP layer using @WebMvcTest.
 *
 * @WebMvcTest(ProductController.class):
 *   - Loads ONLY the web layer (controller, filters, exception handlers)
 *   - Does NOT load the full application context (no DB, no service beans)
 *   - Much faster than @SpringBootTest
 *
 * MockMvc:
 *   - Simulates HTTP requests without actually starting a server
 *   - We can call endpoints and assert on status codes, headers, JSON body
 *
 * @MockBean:
 *   - Creates a Mockito mock of ProductService and registers it as a Spring bean
 *   - The controller will use this mock instead of the real service
 *
 * @Import(GlobalExceptionHandler.class):
 *   - @WebMvcTest doesn't load @RestControllerAdvice automatically in all setups,
 *     so we import it explicitly to test that error handling works correctly
 */
@WebMvcTest(ProductController.class)
@Import(GlobalExceptionHandler.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;  // injects the MockMvc instance for making requests

    @MockBean
    private ProductService productService;  // mock — we control its behaviour in each test

    /**
     * TEST: GET /api/products returns 200 with a paginated body.
     *
     * We create a fake Page, tell the mock service to return it,
     * then verify the response status and JSON structure.
     */
    @Test
    @DisplayName("GET /api/products - should return 200 with paginated products")
    void getAllProducts_shouldReturn200WithPage() throws Exception {
        // Build a fake ProductDTO
        ProductDTO product = ProductDTO.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("999.99"))
                .stock(10)
                .build();

        // Wrap it in a Page object (what the service would return)
        Page<ProductDTO> page = new PageImpl<>(
                List.of(product),                       // content
                PageRequest.of(0, 10),                  // pageable
                1                                        // total elements
        );

        // Tell the mock: when getAllProducts() is called with any Pageable, return our page
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        // Perform the HTTP GET request and assert the response
        mockMvc.perform(get("/api/products")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))

                // Assert HTTP status is 200 OK
                .andExpect(status().isOk())

                // Assert the response content type is JSON
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Assert specific fields in the JSON body
                // "content" is the array in Spring's Page response
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.content[0].price").value(999.99))

                // Assert pagination metadata
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    /**
     * TEST: GET /api/products/999 returns 404 with an error JSON body.
     *
     * We tell the mock service to throw ProductNotFoundException when called with id=999.
     * We then verify that the GlobalExceptionHandler catches it and returns the proper 404 JSON.
     */
    @Test
    @DisplayName("GET /api/products/999 - non-existent product should return 404 error JSON")
    void getProductById_notFound_shouldReturn404() throws Exception {
        // Tell the mock: when getProductById(999) is called, throw our exception
        when(productService.getProductById(999L))
                .thenThrow(new ProductNotFoundException(999L));

        // Perform GET /api/products/999
        mockMvc.perform(get("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON))

                // Assert HTTP status is 404 Not Found
                .andExpect(status().isNotFound())

                // Assert the JSON error response has the required fields
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Product not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/api/products/999"))

                // timestamp should be present (we don't check the exact value — it's dynamic)
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
