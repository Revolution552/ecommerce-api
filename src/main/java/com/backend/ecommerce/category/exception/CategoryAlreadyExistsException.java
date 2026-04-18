// src/main/java/com/backend/ecommerce/category/exception/CategoryAlreadyExistsException.java
package com.backend.ecommerce.category.exception;

public class CategoryAlreadyExistsException extends RuntimeException {
    public CategoryAlreadyExistsException(String message) {
        super(message);
    }
}