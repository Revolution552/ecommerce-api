// src/main/java/com/backend/ecommerce/cart/payload/CartResponseDto.java
package com.backend.ecommerce.cart.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponseDto(
        Long id,
        Long userId,
        String sessionId,
        List<CartItemResponseDto> items,
        Integer totalItems,
        Integer selectedItemsCount,
        BigDecimal subtotal,
        BigDecimal selectedSubtotal,
        BigDecimal totalSavings,
        BigDecimal estimatedTax,
        BigDecimal estimatedShipping,
        BigDecimal estimatedTotal,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime expiresAt,
        boolean isExpired
) {}