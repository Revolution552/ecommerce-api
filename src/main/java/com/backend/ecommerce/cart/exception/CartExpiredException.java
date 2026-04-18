// src/main/java/com/backend/ecommerce/cart/exception/CartExpiredException.java
package com.backend.ecommerce.cart.exception;

public class CartExpiredException extends RuntimeException {
    public CartExpiredException(String message) {
        super(message);
    }
}