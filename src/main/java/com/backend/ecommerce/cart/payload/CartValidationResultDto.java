// src/main/java/com/backend/ecommerce/cart/payload/CartValidationResultDto.java
package com.backend.ecommerce.cart.payload;

import java.util.List;
import java.util.Map;

public record CartValidationResultDto(
        boolean isValid,
        List<String> errors,
        List<CartItemValidationDto> itemValidations,
        Map<Long, Integer> availableStock
) {}