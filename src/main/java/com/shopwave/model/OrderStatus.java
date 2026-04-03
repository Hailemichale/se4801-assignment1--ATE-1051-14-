package com.shopwave.model;

/**
 * Enum representing the possible states of an Order.
 *
 * Using an enum makes it type-safe — you can't accidentally set
 * status to "SHIPED" (typo) because the compiler would catch it.
 *
 * Lifecycle: PENDING -> SHIPPED -> DELIVERED
 *                    -> CANCELLED (can cancel from PENDING)
 */
public enum OrderStatus {
    PENDING,    // Order placed, not yet shipped
    SHIPPED,    // Order is on the way
    DELIVERED,  // Customer received the order
    CANCELLED   // Order was cancelled
}
