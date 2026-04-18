// src/main/java/com/backend/ecommerce/favorites/payload/FavoriteCreateRequest.java
package com.backend.ecommerce.favorites.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FavoriteCreateRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private String notes;
    private Boolean notifyOnSale = false;
    private Boolean notifyOnStock = false;
}