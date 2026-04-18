// src/main/java/com/backend/ecommerce/cart/payload/BulkAddToCartRequest.java
package com.backend.ecommerce.cart.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkAddToCartRequest {

    @NotNull(message = "Items are required")
    private List<AddToCartRequest> items;
}