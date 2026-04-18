// src/main/java/com/backend/ecommerce/favorites/payload/FavoriteListResponseDto.java
package com.backend.ecommerce.favorites.payload;

import java.util.List;

public record FavoriteListResponseDto(
        List<FavoriteResponseDto> favorites,
        Long totalCount,
        Integer totalPages,
        Integer currentPage,
        Integer pageSize
) {}