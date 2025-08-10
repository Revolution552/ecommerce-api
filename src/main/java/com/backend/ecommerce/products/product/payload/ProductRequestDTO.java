package com.backend.ecommerce.products.product.payload;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDTO {

    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Product description is required")
    @Size(min = 10, message = "Product description must be at least 10 characters long")
    private String description;

    @NotNull(message = "Product price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    // This DTO does NOT contain 'imageUrl', 'id', 'user', 'categoryName', or 'shopName'.
    // Those are handled by the backend or are for responses only.
}