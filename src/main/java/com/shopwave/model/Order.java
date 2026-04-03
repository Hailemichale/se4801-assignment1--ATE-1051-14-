package com.shopwave.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Order entity — represents a customer's purchase order.
 *
 * Key design decisions:
 *
 * 1. orphanRemoval = true on the items list:
 *    If we remove an OrderItem from the list and save the Order,
 *    the orphan item is automatically DELETED from the database.
 *    Without this, you'd have "dangling" rows with no parent order.
 *
 * 2. CascadeType.ALL on items:
 *    Any operation on the Order (persist, merge, remove) is cascaded
 *    to its items automatically — no need to save each item separately.
 *
 * 3. @Enumerated(EnumType.STRING):
 *    Stores the enum as a readable string ("PENDING") in the DB
 *    instead of an integer (0). Much easier to read and debug!
 *
 * Note: We avoid using @Data on entities with bidirectional relationships
 * because Lombok's toString/hashCode can cause StackOverflowErrors.
 * Here we use @Getter/@Setter + manual addItem() instead.
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A human-readable, unique order number (e.g. "ORD-20240101-001")
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    // Current status stored as a readable string
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    // Total monetary value of this order
    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // Auto-set on first insert — we never update this
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * The list of items in this order.
     *
     * mappedBy = "order" tells JPA that the "order" field in OrderItem
     * owns this relationship (has the foreign key column).
     *
     * CascadeType.ALL — persist/merge/remove cascades to items.
     * orphanRemoval = true — deleting an item from the list deletes it from the DB.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default  // needed so @Builder doesn't set this to null
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Convenience method — adds a product to this order.
     *
     * It creates an OrderItem, links it back to this Order,
     * and adds it to the list. The price snapshot is taken here.
     *
     * Usage:
     *   order.addItem(product, 3); // add 3 units of the product
     *
     * @param product  the product to add
     * @param quantity how many units
     */
    public void addItem(Product product, int quantity) {
        // Build the OrderItem — price is captured from the product right now
        OrderItem item = OrderItem.builder()
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .order(this)  // link back to this order
                .build();

        // Add to our list
        this.items.add(item);

        // Recalculate total (price × qty for all items)
        recalculateTotal();
    }

    /**
     * Helper: Recomputes totalAmount from the current items list.
     * Called whenever items are added/removed.
     */
    private void recalculateTotal() {
        this.totalAmount = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
