// src/main/java/com/backend/ecommerce/order/model/OrderItem.java
package com.backend.ecommerce.order.model; // Changed package name

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Represents an item within an Order.
 * This class maps to the 'order_items' table in the database.
 * Uses Lombok for boilerplate code (getters, setters, constructors).
 */
@Entity
@Table(name = "order_items")
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for object creation
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO) // Generates a unique ID for each order item
    private Long id;

    @Column(nullable = false)
    private Long productId; // ID of the product ordered

    @Column(nullable = false)
    private Integer quantity; // Quantity of the product

    @Column(nullable = false)
    private BigDecimal price; // Price per unit of the product at the time of order

    // Many order items can belong to one order.
    // LAZY fetch type: The associated Order will be fetched only when explicitly accessed.
    // JOIN column: 'order_id' in the 'order_items' table will store the ID of the parent order.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Reference to the parent Order
}
