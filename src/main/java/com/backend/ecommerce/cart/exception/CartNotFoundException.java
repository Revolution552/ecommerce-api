// src/main/java/com/backend/ecommerce/cart/exception/CartNotFoundException.java
package com.backend.ecommerce.cart.exception;

public class CartNotFoundException extends RuntimeException {
    public CartNotFoundException(String message) {
        super(message);
    }
}