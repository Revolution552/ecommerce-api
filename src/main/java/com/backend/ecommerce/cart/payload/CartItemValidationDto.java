// src/main/java/com/backend/ecommerce/cart/payload/CartItemValidationDto.java
package com.backend.ecommerce.cart.payload;

public record CartItemValidationDto(
        Long cartItemId,
        Long productId,
        String productName,
        Integer requestedQuantity,
        Integer availableQuantity,
        boolean isAvailable,
        String error
) {}