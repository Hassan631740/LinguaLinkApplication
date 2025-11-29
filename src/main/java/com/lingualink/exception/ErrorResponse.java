package com.lingualink.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error response format for all API errors.
 * Provides consistent JSON error structure across the application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    
    /**
     * Timestamp when the error occurred
     */
    private LocalDateTime timestamp;
    
    /**
     * HTTP status code
     */
    private int status;
    
    /**
     * Error type/category (e.g., "Validation Failed", "Resource Not Found")
     */
    private String error;
    
    /**
     * Human-readable error message
     */
    private String message;
    
    /**
     * Path of the request that caused the error
     */
    private String path;
    
    /**
     * Detailed validation errors (field name -> error message)
     * Only included when there are validation errors
     */
    private Map<String, String> validationErrors;
    
    /**
     * Constructor for simple error responses without validation errors
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
    }
    
    /**
     * Constructor for validation error responses
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, Map<String, String> validationErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.validationErrors = validationErrors;
    }
}

