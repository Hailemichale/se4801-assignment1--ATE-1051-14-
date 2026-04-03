package com.shopwave.exception;

/**
 * ProductNotFoundException — thrown when we try to fetch a product
 * that doesn't exist in the database.
 *
 * It extends RuntimeException so:
 *   1. We don't need to declare it in method signatures (unchecked exception)
 *   2. Spring's @Transactional rolls back the transaction on RuntimeException
 *
 * Our GlobalExceptionHandler will catch this and return a 404 response.
 */
public class ProductNotFoundException extends RuntimeException {

    // Store the ID so the handler can include it in the error message
    private final Long productId;

    public ProductNotFoundException(Long id) {
        // Call super with a descriptive message — this is what gets logged
        super("Product not found with id: " + id);
        this.productId = id;
    }

    public Long getProductId() {
        return productId;
    }
}
