// src/main/java/com/backend/ecommerce/cart/payload/CartItemDTO.java
package com.backend.ecommerce.cart.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Data Transfer Object for CartItem.
 * Used for receiving requests to add/update items and sending item details.
 */
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for object creation
public class CartItemDTO {

    private Long id; // Cart Item ID (for response)

    @NotNull(message = "Product ID cannot be null")
    private Long productId; // ID of the product

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private BigDecimal price; // Price of the product (can be null for request, fetched by service)
}
