// src/main/java/com/backend/ecommerce/seller/exception/SellerAlreadyExistsException.java
package com.backend.ecommerce.seller.exception;

public class SellerAlreadyExistsException extends RuntimeException {
    public SellerAlreadyExistsException(String message) {
        super(message);
    }
}