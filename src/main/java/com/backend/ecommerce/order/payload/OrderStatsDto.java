// src/main/java/com/backend/ecommerce/order/payload/OrderStatsDto.java
package com.backend.ecommerce.order.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderStatsDto(
        Long totalOrders,
        BigDecimal totalRevenue,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}