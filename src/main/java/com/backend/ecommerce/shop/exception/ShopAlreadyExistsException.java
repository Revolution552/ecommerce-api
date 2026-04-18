// src/main/java/com/backend/ecommerce/shop/exception/ShopAlreadyExistsException.java
package com.backend.ecommerce.shop.exception;

public class ShopAlreadyExistsException extends RuntimeException {
    public ShopAlreadyExistsException(String message) {
        super(message);
    }
}