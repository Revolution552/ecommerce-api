package com.backend.ecommerce.products.product.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Long shopId;
    private String shopName;

    // This DTO does NOT contain the 'user' object for security reasons.
    // It's used to return a comprehensive view of the product to the client.
}