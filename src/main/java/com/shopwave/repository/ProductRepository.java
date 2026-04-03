package com.shopwave.repository;

import com.shopwave.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * ProductRepository — data access layer for Product entities.
 *
 * Spring Data JPA derives SQL queries from method names automatically!
 * For example:
 *   findByCategoryId(Long id)
 *   → SELECT * FROM products WHERE category_id = ?
 *
 * You don't write the SQL — Spring figures it out from the name.
 * This is called "derived query methods" or "query by naming convention."
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all products belonging to a specific category.
     * SQL: SELECT * FROM products WHERE category_id = ?
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * Find products with a price at or below the given max price.
     * SQL: SELECT * FROM products WHERE price <= ?
     */
    List<Product> findByPriceLessThanEqual(BigDecimal maxPrice);

    /**
     * Case-insensitive search by product name (partial match).
     * SQL: SELECT * FROM products WHERE LOWER(name) LIKE LOWER('%keyword%')
     *
     * "Containing" → LIKE '%...%'
     * "IgnoreCase" → LOWER() on both sides
     */
    List<Product> findByNameContainingIgnoreCase(String keyword);

    /**
     * Find the single most expensive product.
     *
     * "Top"     → LIMIT 1
     * "OrderBy" → ORDER BY
     * "PriceDesc" → price DESC
     *
     * Returns Optional because there might be no products at all.
     */
    Optional<Product> findTopByOrderByPriceDesc();
}
