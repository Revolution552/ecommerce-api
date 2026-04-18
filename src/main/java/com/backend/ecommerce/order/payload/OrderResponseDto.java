// src/main/java/com/backend/ecommerce/order/payload/OrderResponseDto.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.FulfillmentStatus;
import com.backend.ecommerce.order.model.OrderStatus;
import com.backend.ecommerce.order.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(
        Long id,
        String orderNumber,
        Long userId,
        String userEmail,
        String userFullName,
        OrderStatus status,
        PaymentStatus paymentStatus,
        FulfillmentStatus fulfillmentStatus,
        List<OrderItemResponseDto> items,
        OrderShippingResponseDto shipping,
        OrderPaymentResponseDto payment,
        List<OrderStatusHistoryResponseDto> statusHistory,
        BigDecimal subtotal,
        BigDecimal shippingCost,
        BigDecimal tax,
        BigDecimal discount,
        BigDecimal total,
        String notes,
        String customerNotes,
        String couponCode,
        String currency,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime orderedAt,
        LocalDateTime paidAt,
        LocalDateTime shippedAt,
        LocalDateTime deliveredAt,
        LocalDateTime cancelledAt,
        boolean canBeCancelled,
        boolean canBeRefunded
) {}