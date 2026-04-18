// src/main/java/com/backend/ecommerce/order/payload/ShopOrderStatsDto.java
package com.backend.ecommerce.order.payload;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShopOrderStatsDto(
        Long shopId,
        Long totalOrders,
        BigDecimal totalRevenue,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}