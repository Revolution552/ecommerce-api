// src/main/java/com/backend/ecommerce/category/payload/CategoryResponseDto.java
package com.backend.ecommerce.category.payload;

import java.time.LocalDateTime;
import java.util.List;

public record CategoryResponseDto(
        Long id,
        String name,
        String slug,
        String description,
        String iconUrl,
        String imageUrl,
        String bannerUrl,
        Long parentId,
        String parentName,
        Integer level,
        Integer sortOrder,
        Boolean isActive,
        Boolean isFeatured,
        Boolean showInMenu,
        String fullPath,
        String metaTitle,
        String metaDescription,
        String metaKeywords,
        Long productCount,
        List<CategoryResponseDto> children,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}