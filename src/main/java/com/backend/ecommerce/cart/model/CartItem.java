// src/main/java/com/backend/ecommerce/cart/model/CartItem.java
package com.backend.ecommerce.cart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents an item within a shopping cart.
 * This class maps to the 'cart_items' table in the database.
 */
@Entity
@Table(name = "cart_items")
@Data // Generates getters, setters, toString, equals, and hashCode
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for object creation
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrementing Long ID
    private Long id;

    @Column(nullable = false)
    private Long productId; // ID of the product in the cart

    @Column(nullable = false)
    private Integer quantity; // Quantity of the product in the cart

    @Column(nullable = false)
    private BigDecimal price; // Price per unit of the product when added to cart

    // Many cart items can belong to one cart.
    // LAZY fetch type: The associated Cart will be fetched only when explicitly accessed.
    // JOIN column: 'cart_id' in the 'cart_items' table will store the ID of the parent cart.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart; // Reference to the parent Cart
}
