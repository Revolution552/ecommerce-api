// src/main/java/com/backend/ecommerce/category/exception/CategoryNotFoundException.java
package com.backend.ecommerce.category.exception;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
}