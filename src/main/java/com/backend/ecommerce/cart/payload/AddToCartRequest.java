// src/main/java/com/backend/ecommerce/cart/payload/AddToCartRequest.java
package com.backend.ecommerce.cart.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddToCartRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;

    private String notes;
    private Boolean isSelected = true;
}