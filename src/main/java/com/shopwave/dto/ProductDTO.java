package com.shopwave.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ProductDTO — Data Transfer Object for returning product info to clients.
 *
 * Why a DTO and not the entity directly?
 *   - Entities can have lazy-loaded JPA proxies that break JSON serialization
 *   - DTOs let us control exactly what data is exposed (e.g. hide internal fields)
 *   - They decouple the API contract from the database schema
 *
 * This is what the client sees in the API response body.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;

    // We include category info directly (flattened) instead of embedding
    // the full Category object — keeps the response clean and simple
    private Long categoryId;
    private String categoryName;

    private LocalDateTime createdAt;
}
