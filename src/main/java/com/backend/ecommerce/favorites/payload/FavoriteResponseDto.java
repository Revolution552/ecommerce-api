// src/main/java/com/backend/ecommerce/favorites/payload/FavoriteResponseDto.java
package com.backend.ecommerce.favorites.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record FavoriteResponseDto(
        Long id,
        Long userId,
        Long productId,
        String productName,
        String productSlug,
        BigDecimal productPrice,
        BigDecimal compareAtPrice,
        BigDecimal discountPercentage,
        String productMainImage,
        Integer productQuantity,
        Boolean isInStock,
        Boolean isOnSale,
        String shopName,
        String shopSlug,
        String notes,
        Boolean notifyOnSale,
        Boolean notifyOnStock,
        LocalDateTime createdAt
) {}