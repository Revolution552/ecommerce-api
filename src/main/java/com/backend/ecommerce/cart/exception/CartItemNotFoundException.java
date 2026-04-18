// src/main/java/com/backend/ecommerce/cart/exception/CartItemNotFoundException.java
package com.backend.ecommerce.cart.exception;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(String message) {
        super(message);
    }
}