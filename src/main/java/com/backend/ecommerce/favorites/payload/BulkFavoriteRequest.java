// src/main/java/com/backend/ecommerce/favorites/payload/BulkFavoriteRequest.java
package com.backend.ecommerce.favorites.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class BulkFavoriteRequest {

    @NotNull(message = "Product IDs are required")
    private List<Long> productIds;
}