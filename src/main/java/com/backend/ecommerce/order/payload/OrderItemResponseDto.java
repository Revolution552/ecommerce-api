// src/main/java/com/backend/ecommerce/order/payload/OrderItemResponseDto.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.FulfillmentStatus;

import java.math.BigDecimal;

public record OrderItemResponseDto(
        Long id,
        Long productId,
        String productName,
        String productSku,
        String productImage,
        String productSlug,
        Long shopId,
        String shopName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal compareAtPrice,
        BigDecimal totalPrice,
        BigDecimal discount,
        BigDecimal tax,
        BigDecimal savings,
        String notes,
        FulfillmentStatus fulfillmentStatus
) {}