package com.restaurant.pos.expense.exception;

public class IdempotencyStoreException extends RuntimeException {
    
    public IdempotencyStoreException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public IdempotencyStoreException(String message) {
        super(message);
    }
}
