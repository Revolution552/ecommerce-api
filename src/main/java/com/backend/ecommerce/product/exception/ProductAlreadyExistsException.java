// src/main/java/com/backend/ecommerce/product/exception/ProductAlreadyExistsException.java
package com.backend.ecommerce.product.exception;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String message) {
        super(message);
    }
}