// src/main/java/com/backend/ecommerce/shop/exception/ShopNotApprovedException.java
package com.backend.ecommerce.shop.exception;

public class ShopNotApprovedException extends RuntimeException {
    public ShopNotApprovedException(String message) {
        super(message);
    }
}