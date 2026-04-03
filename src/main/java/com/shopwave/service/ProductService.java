package com.shopwave.service;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.exception.ProductNotFoundException;
import com.shopwave.mapper.ProductMapper;
import com.shopwave.model.Category;
import com.shopwave.model.Product;
import com.shopwave.repository.CategoryRepository;
import com.shopwave.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ProductService — contains all the business logic for products.
 *
 * @Service — marks this as a Spring service bean
 * @Transactional — every method runs inside a database transaction by default.
 *   If an exception is thrown mid-method, the entire transaction rolls back.
 *   This prevents partial saves (e.g. product saved but stock not updated).
 *
 * @RequiredArgsConstructor — Lombok generates a constructor for all 'final' fields.
 *   This is the recommended way to do constructor injection instead of @Autowired.
 *
 * @Slf4j — Lombok injects a 'log' variable for logging (uses SLF4J under the hood)
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    // Spring injects these automatically via constructor injection (thanks to @RequiredArgsConstructor)
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    /**
     * Creates and saves a new product.
     *
     * Steps:
     * 1. If a categoryId is provided, look up the category (throw if not found)
     * 2. Convert the request DTO → Product entity
     * 3. Save to DB
     * 4. Convert saved entity → ProductDTO and return
     *
     * @param request the product data from the client
     * @return the saved product as a DTO
     */
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("Creating product with name: {}", request.getName());

        // Look up category only if one was specified
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Category not found with id: " + request.getCategoryId()));
        }

        // Map DTO → Entity
        Product product = productMapper.toEntity(request, category);

        // Save entity to the database — returns the saved entity with id populated
        Product savedProduct = productRepository.save(product);

        log.info("Product created with id: {}", savedProduct.getId());

        // Map Entity → DTO for the response
        return productMapper.toDTO(savedProduct);
    }

    /**
     * Returns a paginated list of all products.
     *
     * Pagination keeps the response manageable when there are thousands of products.
     * The client passes ?page=0&size=10 in the query string.
     *
     * @Transactional(readOnly = true) tells Hibernate this is a read-only operation
     * which allows some optimizations (e.g., no dirty checking, can use read replicas)
     *
     * @param pageable pagination parameters (page number, page size, sort)
     * @return a Page object containing the products and pagination metadata
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("Fetching all products, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());

        // findAll(pageable) returns a Page<Product> — we map each Product to ProductDTO
        return productRepository.findAll(pageable)
                .map(productMapper::toDTO);  // method reference (equivalent to p -> productMapper.toDTO(p))
    }

    /**
     * Finds a product by its ID, or throws ProductNotFoundException.
     *
     * The orElseThrow pattern is idiomatic Java — instead of:
     *   Optional<Product> opt = repo.findById(id);
     *   if (!opt.isPresent()) throw new ...;
     *   return opt.get();
     * We do it all in one line!
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return productMapper.toDTO(product);
    }

    /**
     * Searches products by an optional keyword and/or optional max price.
     *
     * Strategy:
     *   - If both provided: filter by keyword first, then filter by price in Java
     *   - If only keyword: use name search
     *   - If only maxPrice: use price filter
     *   - If neither: return all products (we could paginate, but the spec says List<>)
     *
     * In a real app you'd write a custom @Query or use Specifications for this,
     * but this approach is simple and readable for an assignment.
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword, BigDecimal maxPrice) {
        log.debug("Searching products — keyword: {}, maxPrice: {}", keyword, maxPrice);

        List<Product> results;

        if (keyword != null && !keyword.isBlank() && maxPrice != null) {
            // Both filters: search by name, then filter by price in Java
            results = productRepository.findByNameContainingIgnoreCase(keyword)
                    .stream()
                    .filter(p -> p.getPrice().compareTo(maxPrice) <= 0)
                    .collect(Collectors.toList());

        } else if (keyword != null && !keyword.isBlank()) {
            // Keyword only
            results = productRepository.findByNameContainingIgnoreCase(keyword);

        } else if (maxPrice != null) {
            // Price filter only
            results = productRepository.findByPriceLessThanEqual(maxPrice);

        } else {
            // No filter — return everything
            results = productRepository.findAll();
        }

        // Convert each Product entity to a ProductDTO
        return results.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Adjusts a product's stock by adding delta (can be negative for sales).
     *
     * Example:
     *   current stock = 10, delta = +5  → new stock = 15 (restock)
     *   current stock = 10, delta = -3  → new stock = 7  (3 items sold)
     *   current stock = 2,  delta = -5  → throws IllegalArgumentException (can't go negative!)
     *
     * @param id    the product to update
     * @param delta how much to change the stock (positive or negative)
     * @return the updated product as DTO
     */
    public ProductDTO updateStock(Long id, int delta) {
        log.info("Updating stock for product id: {}, delta: {}", id, delta);

        // First, make sure the product exists
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        int newStock = product.getStock() + delta;

        // Business rule: stock can never go below zero
        if (newStock < 0) {
            throw new IllegalArgumentException(
                    "Insufficient stock. Current stock: " + product.getStock()
                    + ", attempted change: " + delta
                    + ". Stock cannot go negative.");
        }

        product.setStock(newStock);

        // Save the updated entity — @Transactional means this is committed when the method returns
        Product updatedProduct = productRepository.save(product);

        log.info("Stock updated for product id: {}. New stock: {}", id, newStock);

        return productMapper.toDTO(updatedProduct);
    }
}
