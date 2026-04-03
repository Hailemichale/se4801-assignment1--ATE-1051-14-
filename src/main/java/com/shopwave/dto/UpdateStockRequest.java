package com.shopwave.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateStockRequest — request body for the PATCH /api/products/{id}/stock endpoint.
 *
 * The "delta" can be:
 *   - Positive: restocking (e.g. +50 units arrived)
 *   - Negative: sale (e.g. -3 units sold)
 *
 * The service will validate that final stock doesn't go below zero.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequest {
    // delta can be negative (sale) or positive (restock)
    private int delta;
}
