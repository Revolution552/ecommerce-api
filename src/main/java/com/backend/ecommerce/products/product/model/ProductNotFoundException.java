package com.backend.ecommerce.products.product.model;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long productId) {
        super("Product not found with ID: " + productId);
    }

    public ProductNotFoundException(String message) {
        super(message);
    }
}
