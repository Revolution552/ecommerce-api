// src/main/java/com/backend/ecommerce/cart/model/Cart.java
package com.backend.ecommerce.cart.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a shopping cart in the e-commerce system.
 * Each user has one active cart.
 * This class maps to the 'carts' table in the database.
 */
@Entity
@Table(name = "carts")
@Data
@NoArgsConstructor // Generates a no-argument constructor
@AllArgsConstructor // Generates a constructor with all fields
@Builder // Provides a builder pattern for object creation
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrementing Long ID
    private Long id;

    @Column(nullable = false, unique = true) // Each user has one unique cart
    private Long userId;

    @Column(nullable = false)
    @Builder.Default // Ensures default value is used when using the builder
    private LocalDateTime createdAt = LocalDateTime.now(); // Timestamp for cart creation

    @Column(nullable = false)
    @Builder.Default // Ensures default value is used when using the builder
    private LocalDateTime updatedAt = LocalDateTime.now(); // Timestamp for last update

    // One cart can have many cart items.
    // CascadeType.ALL: Operations (persist, merge, remove) on Cart will cascade to CartItem.
    // orphanRemoval = true: If a CartItem is removed from the list, it will be deleted from the database.
    // mappedBy: Indicates that the 'cart' field in CartItem is the owning side of the relationship.
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default // Ensures that the default value is used when using the builder pattern
    private List<CartItem> cartItems = new ArrayList<>();

    /**
     * Helper method to add a CartItem to the cart and set its parent cart.
     * @param item The CartItem to add.
     */
    public void addCartItem(CartItem item) {
        if (this.cartItems == null) {
            this.cartItems = new ArrayList<>();
        }
        this.cartItems.add(item);
        item.setCart(this); // Set the parent cart for the cart item
        this.setUpdatedAt(LocalDateTime.now()); // Update timestamp on item modification
    }

    /**
     * Helper method to remove a CartItem from the cart.
     * @param item The CartItem to remove.
     */
    public void removeCartItem(CartItem item) {
        if (this.cartItems != null) {
            this.cartItems.remove(item);
            item.setCart(null); // Remove the parent cart reference
            this.setUpdatedAt(LocalDateTime.now()); // Update timestamp on item modification
        }
    }

    /**
     * Custom setter for cartItems to ensure bidirectional relationship is maintained.
     * Lombok's @Data will generate a default setter, but we override it here
     * to ensure `item.setCart(this)` is called for each item.
     * @param cartItems The list of CartItem entities for this cart.
     */
    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems.clear(); // Clear existing items to avoid duplicates
        if (cartItems != null) {
            for (CartItem item : cartItems) {
                this.addCartItem(item); // Use helper method to add and set parent
            }
        }
        this.setUpdatedAt(LocalDateTime.now()); // Update timestamp on cart modification
    }
}
