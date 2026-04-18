// src/main/java/com/backend/ecommerce/order/payload/OrderStatusHistoryResponseDto.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.OrderStatus;

import java.time.LocalDateTime;

public record OrderStatusHistoryResponseDto(
        Long id,
        OrderStatus previousStatus,
        OrderStatus newStatus,
        String notes,
        String changedBy,
        LocalDateTime createdAt
) {}