package com.shopwave.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Category entity — represents a product category (e.g. "Electronics", "Clothing").
 *
 * JPA annotations explained:
 *   @Entity     — marks this class as a database table
 *   @Table      — lets us specify the table name explicitly
 *   @Id         — marks the primary key
 *   @GeneratedValue — auto-increment strategy
 *   @Column     — maps a field to a column (nullable, length, etc.)
 *
 * Lombok annotations:
 *   @Data               — generates getters, setters, equals, hashCode, toString
 *   @Builder            — generates a builder pattern (Category.builder().name("...").build())
 *   @NoArgsConstructor  — JPA requires a no-arg constructor
 *   @AllArgsConstructor — needed by @Builder
 */
@Entity
@Table(name = "categories")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    // Primary key — auto-incremented by the database
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Category name — cannot be blank (enforced by Bean Validation)
    @NotBlank(message = "Category name cannot be blank")
    @Column(nullable = false)
    private String name;

    // Optional description of the category
    @Column(columnDefinition = "TEXT")
    private String description;
}
