// src/main/java/com/backend/ecommerce/favorites/payload/FavoriteUpdateRequest.java
package com.backend.ecommerce.favorites.payload;

import lombok.Data;

@Data
public class FavoriteUpdateRequest {
    private String notes;
    private Boolean notifyOnSale;
    private Boolean notifyOnStock;
}