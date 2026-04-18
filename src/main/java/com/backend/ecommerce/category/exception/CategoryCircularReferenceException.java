// src/main/java/com/backend/ecommerce/category/exception/CategoryCircularReferenceException.java
package com.backend.ecommerce.category.exception;

public class CategoryCircularReferenceException extends RuntimeException {
    public CategoryCircularReferenceException(String message) {
        super(message);
    }
}