// src/main/java/com/backend/ecommerce/order/exception/InsufficientStockException.java
package com.backend.ecommerce.order.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}