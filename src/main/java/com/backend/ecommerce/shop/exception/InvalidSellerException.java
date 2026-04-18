// src/main/java/com/backend/ecommerce/shop/exception/InvalidSellerException.java
package com.backend.ecommerce.shop.exception;

public class InvalidSellerException extends RuntimeException {
    public InvalidSellerException(String message) {
        super(message);
    }
}