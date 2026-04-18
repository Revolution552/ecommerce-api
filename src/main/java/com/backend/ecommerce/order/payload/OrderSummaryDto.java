// src/main/java/com/backend/ecommerce/order/payload/OrderSummaryDto.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.OrderStatus;
import com.backend.ecommerce.order.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long id,
        String orderNumber,
        OrderStatus status,
        PaymentStatus paymentStatus,
        Integer itemCount,
        BigDecimal total,
        LocalDateTime createdAt
) {}