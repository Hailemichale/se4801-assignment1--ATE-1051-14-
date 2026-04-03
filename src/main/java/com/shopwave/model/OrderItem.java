package com.shopwave.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * OrderItem entity — represents a single line item within an Order.
 *
 * Think of it like a row on a receipt:
 *   "iPhone 15 x 2 units @ $999.00 each"
 *    ^product    ^qty    ^unitPrice
 *
 * The unitPrice is stored separately from the product's current price
 * because prices can change over time — we want to record what the
 * customer actually paid at the time of purchase.
 *
 * Relationship:
 *   - ManyToOne Order:   many items belong to one order (owns the FK column)
 *   - ManyToOne Product: many order items can reference the same product
 */
@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The parent order. This is the "owning" side of the relationship
     * because order_items table holds the foreign key (order_id).
     *
     * @JsonIgnore prevents infinite recursion when Jackson serializes:
     * Order → items → item.order → items → ... (would loop forever)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    // Which product this line item is for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // How many units of this product were ordered
    @Column(nullable = false)
    private Integer quantity;

    // The price per unit at the time of the order (snapshot!)
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
}
