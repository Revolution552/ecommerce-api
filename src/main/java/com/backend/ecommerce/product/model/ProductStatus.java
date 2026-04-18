// src/main/java/com/backend/ecommerce/product/model/ProductStatus.java
package com.backend.ecommerce.product.model;

public enum ProductStatus {
    DRAFT("Draft"),
    PENDING_REVIEW("Pending Review"),
    PUBLISHED("Published"),
    ARCHIVED("Archived"),
    OUT_OF_STOCK("Out of Stock"),
    DISCONTINUED("Discontinued");

    private final String description;

    ProductStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}