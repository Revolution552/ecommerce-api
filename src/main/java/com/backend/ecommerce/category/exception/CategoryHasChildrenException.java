// src/main/java/com/backend/ecommerce/category/exception/CategoryHasChildrenException.java
package com.backend.ecommerce.category.exception;

public class CategoryHasChildrenException extends RuntimeException {
    public CategoryHasChildrenException(String message) {
        super(message);
    }
}