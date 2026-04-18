// src/main/java/com/backend/ecommerce/shop/exception/ShopNotFoundException.java
package com.backend.ecommerce.shop.exception;

public class ShopNotFoundException extends RuntimeException {
    public ShopNotFoundException(String message) {
        super(message);
    }
}