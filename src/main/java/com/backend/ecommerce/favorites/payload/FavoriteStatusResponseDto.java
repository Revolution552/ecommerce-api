// src/main/java/com/backend/ecommerce/favorites/payload/FavoriteStatusResponseDto.java
package com.backend.ecommerce.favorites.payload;

public record FavoriteStatusResponseDto(
        Long productId,
        Boolean isFavorite,
        Long favoriteId
) {}