// src/main/java/com/backend/ecommerce/cart/payload/CartItemResponseDto.java
package com.backend.ecommerce.cart.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CartItemResponseDto(
        Long id,
        Long cartId,
        Long productId,
        String productName,
        String productSlug,
        String productMainImage,
        Long shopId,
        String shopName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal compareAtPrice,
        BigDecimal totalPrice,
        BigDecimal savings,
        Boolean isInStock,
        Integer maxAvailableQuantity,
        String notes,
        Boolean isSelected,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}