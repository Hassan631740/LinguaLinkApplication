package com.lingualink.exception;

/**
 * Exception thrown when a resource conflict occurs (e.g., duplicate email, unique constraint violation).
 * Results in HTTP 409 Conflict status.
 */
public class ResourceConflictException extends RuntimeException {
    public ResourceConflictException(String message) {
        super(message);
    }

    public ResourceConflictException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s with %s '%s' already exists", resourceName, fieldName, fieldValue));
    }
}

