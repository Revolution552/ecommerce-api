package com.backend.ecommerce.products.product.payload;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewDTO {
    @NotNull
    private Long productId;

    @NotNull
    private Long userId;

    @Min(1)
    @Max(5)
    private int rating;

    private String comment;

    // Getters and Setters
}
