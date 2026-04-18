// src/main/java/com/backend/ecommerce/order/payload/OrderPaymentResponseDto.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.PaymentMethod;
import com.backend.ecommerce.order.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderPaymentResponseDto(
        Long id,
        PaymentMethod paymentMethod,
        String transactionId,
        BigDecimal amount,
        String currency,
        PaymentStatus status,
        String paymentDetails,
        LocalDateTime paidAt,
        BigDecimal refundAmount,
        LocalDateTime refundedAt
) {}