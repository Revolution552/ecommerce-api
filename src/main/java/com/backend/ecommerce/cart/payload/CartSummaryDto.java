// src/main/java/com/backend/ecommerce/cart/payload/CartSummaryDto.java
package com.backend.ecommerce.cart.payload;

import java.math.BigDecimal;

public record CartSummaryDto(
        Long cartId,
        Integer totalItems,
        Integer selectedItemsCount,
        BigDecimal subtotal,
        BigDecimal selectedSubtotal,
        BigDecimal totalSavings
) {}