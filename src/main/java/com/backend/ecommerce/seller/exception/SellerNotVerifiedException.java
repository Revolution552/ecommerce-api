// src/main/java/com/backend/ecommerce/seller/exception/SellerNotVerifiedException.java
package com.backend.ecommerce.seller.exception;

public class SellerNotVerifiedException extends RuntimeException {
    public SellerNotVerifiedException(String message) {
        super(message);
    }
}