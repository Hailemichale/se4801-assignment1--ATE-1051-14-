package com.shopwave.controller;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.dto.UpdateStockRequest;
import com.shopwave.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * ProductController — the REST API layer for product operations.
 *
 * @RestController = @Controller + @ResponseBody
 *   → Every method return value is automatically serialized to JSON.
 *   → No need for @ResponseBody on individual methods.
 *
 * @RequestMapping("/api") — all endpoints start with /api
 *   So our product endpoints are at /api/products/...
 *
 * @RequiredArgsConstructor — constructor injection for ProductService
 *
 * Controllers should be THIN:
 *   - Only handle HTTP concerns (request parsing, status codes, validation trigger)
 *   - Delegate ALL business logic to the service layer
 *   - Should not directly interact with repositories
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products?page=0&size=10
     *
     * Returns a paginated list of all products.
     *
     * @PageableDefault sets default values if the client doesn't specify:
     *   page=0 (first page), size=10 (10 items per page)
     *
     * Spring automatically parses ?page=X&size=Y&sort=field,direction into Pageable.
     */
    @GetMapping("/products")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<ProductDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);  // 200 OK
    }

    /**
     * GET /api/products/{id}
     *
     * Returns a single product by ID.
     * If not found: ProductNotFoundException → GlobalExceptionHandler → 404 response
     *
     * @PathVariable binds the {id} from the URL to the method parameter
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);  // 200 OK
    }

    /**
     * POST /api/products
     *
     * Creates a new product. Expects a JSON body matching CreateProductRequest.
     *
     * @Valid triggers Bean Validation on the request body.
     *   If validation fails, Spring throws MethodArgumentNotValidException
     *   which our handler returns as a 400 response with error details.
     *
     * @RequestBody tells Spring to deserialize the JSON body into the object.
     *
     * Returns 201 Created (not 200 OK) — important distinction!
     *   201 means a new resource was successfully created.
     */
    @PostMapping("/products")
    public ResponseEntity<ProductDTO> createProduct(
            @Valid @RequestBody CreateProductRequest request) {

        ProductDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);  // 201 Created
    }

    /**
     * GET /api/products/search?keyword=phone&maxPrice=500
     *
     * Searches products by keyword (partial name match) and/or max price.
     * Both parameters are optional — omitting them returns all products.
     *
     * @RequestParam(required = false) — parameter is optional.
     *   If not provided, the value defaults to null.
     */
    @GetMapping("/products/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal maxPrice) {

        List<ProductDTO> results = productService.searchProducts(keyword, maxPrice);
        return ResponseEntity.ok(results);  // 200 OK
    }

    /**
     * PATCH /api/products/{id}/stock
     *
     * Adjusts product stock by a delta (positive or negative).
     * PATCH is appropriate here — we're partially updating one field.
     *
     * Request body: { "delta": -5 }
     *   → stock goes down by 5 (3 items sold)
     *
     * Possible responses:
     *   200 OK  — stock updated successfully
     *   400 Bad Request — delta would make stock negative
     *   404 Not Found — product doesn't exist
     */
    @PatchMapping("/products/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(
            @PathVariable Long id,
            @RequestBody UpdateStockRequest request) {

        ProductDTO updated = productService.updateStock(id, request.getDelta());
        return ResponseEntity.ok(updated);  // 200 OK
    }
}
