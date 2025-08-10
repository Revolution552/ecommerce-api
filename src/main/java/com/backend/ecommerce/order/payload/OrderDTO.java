// src/main/java/com/backend/ecommerce/order/payload/OrderDTO.java
package com.backend.ecommerce.order.payload; // Changed package to payload

import com.backend.ecommerce.order.model.OrderStatus;
import jakarta.validation.constraints.DecimalMin;
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
 * Data Transfer Object for Order.
 * Used for receiving order creation requests and sending order details responses.
 * Uses Lombok for boilerplate code (getters, setters, constructors).
 */
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for object creation
public class OrderDTO {

    private Long id; // Order ID remains UUID

    @NotNull(message = "User ID cannot be null")
    private Long userId; // Changed from UUID to Long

    private LocalDateTime orderDate; // For response, set by system

    private BigDecimal totalAmount;

    private OrderStatus status; // For response, or for updating status

    @NotNull(message = "Order items cannot be null")
    @Size(min = 1, message = "Order must contain at least one item")
    private List<OrderItemDTO> orderItems;
}
