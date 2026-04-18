// src/main/java/com/backend/ecommerce/product/exception/InsufficientStockException.java
package com.backend.ecommerce.product.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}