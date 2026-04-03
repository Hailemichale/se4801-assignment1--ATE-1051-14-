package com.shopwave.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * CreateProductRequest — the request body when creating a new product.
 *
 * Bean Validation annotations (@NotBlank, @Positive, etc.) are used here
 * so the controller can simply call @Valid and Spring handles validation.
 *
 * If validation fails, Spring throws MethodArgumentNotValidException,
 * which our GlobalExceptionHandler will catch and format as a 400 response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Product name is required")
    private String name;

    // Description is optional — no @NotBlank here
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be greater than 0")
    private BigDecimal price;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    // Category is optional — product can exist without one
    private Long categoryId;
}
