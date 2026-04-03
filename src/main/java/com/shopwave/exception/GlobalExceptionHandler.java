package com.shopwave.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler — a central place to handle exceptions across ALL controllers.
 *
 * @RestControllerAdvice is a combination of @ControllerAdvice + @ResponseBody.
 * It means: "intercept exceptions from any controller and return a JSON response."
 *
 * Instead of putting try-catch blocks in every controller method,
 * we define handlers here once and they apply everywhere.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ProductNotFoundException — return 404 Not Found.
     *
     * Called automatically when any controller throws ProductNotFoundException.
     *
     * @param ex      the thrown exception
     * @param request the HTTP request that caused it (we need the URI for the "path" field)
     * @return JSON ErrorResponse with 404 status
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFound(
            ProductNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),        // 404
                HttpStatus.NOT_FOUND.getReasonPhrase(), // "Not Found"
                ex.getMessage(),                        // "Product not found with id: 99"
                request.getRequestURI()                // "/api/products/99"
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Handle validation errors — return 400 Bad Request.
     *
     * Spring throws MethodArgumentNotValidException when @Valid fails on a request body.
     * We collect all field error messages and join them into one string.
     *
     * Example: "name: Product name is required; price: Price must be greater than 0"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        // Collect all field-level error messages
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),           // 400
                HttpStatus.BAD_REQUEST.getReasonPhrase(), // "Bad Request"
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Handle IllegalArgumentException — return 400 Bad Request.
     *
     * This is thrown by ProductService.updateStock() when delta would make stock negative.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * Catch-all handler — return 500 Internal Server Error for anything unexpected.
     *
     * Good practice: never let the client see raw stack traces!
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred. Please try again later.",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
