// src/main/java/com/backend/ecommerce/order/payload/OrderShippingResponseDto.java
package com.backend.ecommerce.order.payload;

import java.time.LocalDateTime;

public record OrderShippingResponseDto(
        Long id,
        String fullName,
        String email,
        String phone,
        String addressLine1,
        String addressLine2,
        String city,
        String state,
        String postalCode,
        String country,
        String shippingMethod,
        String trackingNumber,
        String trackingUrl,
        String carrier,
        LocalDateTime estimatedDelivery,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt
) {}