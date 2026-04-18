// src/main/java/com/backend/ecommerce/cart/payload/UpdateCartItemRequest.java
package com.backend.ecommerce.cart.payload;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateCartItemRequest {

    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity;

    private String notes;
    private Boolean isSelected;
}