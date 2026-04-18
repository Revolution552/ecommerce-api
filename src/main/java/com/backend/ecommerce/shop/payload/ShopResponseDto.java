// src/main/java/com/backend/ecommerce/shop/payload/ShopResponseDto.java
package com.backend.ecommerce.shop.payload;

import com.backend.ecommerce.shop.model.ShopStatus;

import java.time.LocalDateTime;

public record ShopResponseDto(
        Long id,
        String name,
        String slug,
        String description,
        String logoUrl,
        String bannerUrl,
        String address,
        String city,
        String state,
        String country,
        String postalCode,
        String phone,
        String email,
        String website,
        Long sellerId,
        String sellerBusinessName,
        String sellerEmail,
        ShopStatus status,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime approvedAt,
        Boolean isActive,
        Boolean isFeatured,
        Double rating,
        Integer totalReviews,
        Integer totalProducts,
        Integer totalSales
) {}