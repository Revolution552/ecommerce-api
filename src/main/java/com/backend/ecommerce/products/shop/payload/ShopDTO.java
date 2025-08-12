package com.backend.ecommerce.products.shop.payload;

import com.backend.ecommerce.products.shop.model.Shop;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopDTO {

    private Long id;

    @NotEmpty(message = "Shop name is required")
    private String name;

    @NotEmpty(message = "Location is required")
    private String location;

    @NotEmpty(message = "Description is required")
    private String description;

    @NotNull(message = "User ID is required")
    private Long userId;

    private String logoUrl; // New field for the shop's logo URL
}