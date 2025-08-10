// src/main/java/com/backend/ecommerce/cart/payload/CartDTO.java
package com.backend.ecommerce.cart.payload;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Cart.
 * Used for sending cart details responses.
 */
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for object creation
public class CartDTO {

    private Long id; // Cart ID

    @NotNull(message = "User ID cannot be null")
    private Long userId; // User ID associated with the cart

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private BigDecimal totalAmount;

    @Valid
    @NotNull(message = "Cart items cannot be null")
    @Size(min = 1, message = "Cart must contain at least one item")
    private List<CartItemDTO> cartItems; // List of items in the cart
}
