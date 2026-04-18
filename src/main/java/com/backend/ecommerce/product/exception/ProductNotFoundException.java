// src/main/java/com/backend/ecommerce/product/exception/ProductNotFoundException.java
package com.backend.ecommerce.product.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }
}