package com.shopwave.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ErrorResponse — the standard JSON shape returned by our API for all errors.
 *
 * Example response body:
 * {
 *   "timestamp": "2024-01-15T10:30:00",
 *   "status": 404,
 *   "error": "Not Found",
 *   "message": "Product not found with id: 99",
 *   "path": "/api/products/99"
 * }
 *
 * Having a consistent error format makes life much easier for frontend
 * developers — they always know exactly what fields to look for.
 */
@Data
@AllArgsConstructor
public class ErrorResponse {

    // When the error occurred
    private LocalDateTime timestamp;

    // HTTP status code (e.g. 404, 400, 500)
    private int status;

    // Human-readable HTTP status reason (e.g. "Not Found", "Bad Request")
    private String error;

    // Specific error message describing what went wrong
    private String message;

    // The request path that caused the error (e.g. "/api/products/99")
    private String path;
}
