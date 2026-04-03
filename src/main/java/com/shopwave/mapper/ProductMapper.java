package com.shopwave.mapper;

import com.shopwave.dto.CreateProductRequest;
import com.shopwave.dto.ProductDTO;
import com.shopwave.model.Category;
import com.shopwave.model.Product;
import org.springframework.stereotype.Component;

/**
 * ProductMapper — converts between Product entities and DTOs.
 *
 * Why not use the entity directly in the API?
 *   - Lazy-loaded JPA proxies can cause "No Session" errors during JSON serialization
 *   - We want to control exactly what fields are exposed
 *   - DTOs create a clean separation: the DB model can change without breaking the API
 *
 * We're doing this manually (no MapStruct library) so it's easy to read and understand.
 * In a larger project you'd use MapStruct to auto-generate these methods.
 *
 * @Component makes this a Spring bean — we can @Autowire it into the service.
 */
@Component
public class ProductMapper {

    /**
     * Converts a Product entity into a ProductDTO (what the API sends back to the client).
     *
     * Note how we safely handle the nullable category — product might not have one.
     */
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }

        // Use the builder pattern (from @Builder) to construct the DTO cleanly
        ProductDTO.ProductDTOBuilder builder = ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .createdAt(product.getCreatedAt());

        // Only include category data if the product has a category assigned
        if (product.getCategory() != null) {
            builder.categoryId(product.getCategory().getId());
            builder.categoryName(product.getCategory().getName());
        }

        return builder.build();
    }

    /**
     * Converts a CreateProductRequest into a new Product entity (ready to be saved).
     *
     * The category is set separately because we need to look it up from the DB first.
     * createdAt is set automatically by @CreationTimestamp, so we don't set it here.
     */
    public Product toEntity(CreateProductRequest request, Category category) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)  // may be null if no categoryId was provided
                .build();
    }
}
