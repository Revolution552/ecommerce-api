// src/main/java/com/backend/ecommerce/cart/exception/InvalidQuantityException.java
package com.backend.ecommerce.cart.exception;

public class InvalidQuantityException extends RuntimeException {
    public InvalidQuantityException(String message) {
        super(message);
    }
}