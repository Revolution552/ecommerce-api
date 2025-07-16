package com.backend.ecommerce.products.product.payload;

import com.backend.ecommerce.user.model.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDTO {

    @NotNull(message = "Product name is required")
    private String name;

    @NotNull(message = "Product description is required")
    private String description;

    @NotNull(message = "Product price is required")
    @Positive(message = "Price must be a positive value")
    private Double price;

    private String imageUrl;

    @NotNull(message = "Category ID is required")
    @Min(value = 1, message = "Category ID must be a positive integer")
    private Long categoryId;

    @NotNull(message = "Shop ID is required")
    @Min(value = 1, message = "Shop ID must be a positive integer")
    private Long shopId;

    private Long Id;

    private User user;

    private String categoryName;  // New field to show category name

    private String shopName;  // New field to show shop name

    // Lombok's @Getter and @Setter will automatically generate getter and setter methods.
}
