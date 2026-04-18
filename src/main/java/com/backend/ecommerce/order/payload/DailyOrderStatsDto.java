// src/main/java/com/backend/ecommerce/order/payload/DailyOrderStatsDto.java
package com.backend.ecommerce.order.payload;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyOrderStatsDto(
        LocalDate date,
        Long orderCount,
        BigDecimal totalRevenue
) {}