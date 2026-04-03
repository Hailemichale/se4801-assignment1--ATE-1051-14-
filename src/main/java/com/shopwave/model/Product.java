package com.shopwave.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product entity — represents an item available for purchase in the store.
 *
 * Relationships:
 *   - ManyToOne Category: many products can belong to one category
 *     e.g. iPhone, Samsung Galaxy → both in "Electronics" category
 *
 * Note on BigDecimal:
 *   We use BigDecimal for price (not double/float) because floating-point
 *   arithmetic can introduce tiny rounding errors — unacceptable for money!
 */
@Entity
@Table(name = "products")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Product name — required
    @NotBlank(message = "Product name is required")
    @Column(nullable = false)
    private String name;

    // Optional product description
    @Column(columnDefinition = "TEXT")
    private String description;

    // Price must be positive (> 0). BigDecimal(10,2) = up to 10 digits, 2 decimal places
    @Positive(message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // Stock quantity — cannot be negative (you can't have -3 items in stock)
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    // Many products belong to one category.
    // @JoinColumn specifies the foreign key column name in the products table.
    // FetchType.LAZY means the category is only loaded from DB when you actually access it.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Automatically set to the current time when the record is first inserted.
    // @CreationTimestamp is a Hibernate annotation (not standard JPA).
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
