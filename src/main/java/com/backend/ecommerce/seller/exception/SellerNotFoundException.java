// src/main/java/com/backend/ecommerce/seller/exception/SellerNotFoundException.java
package com.backend.ecommerce.seller.exception;

public class SellerNotFoundException extends RuntimeException {
    public SellerNotFoundException(String message) {
        super(message);
    }
}