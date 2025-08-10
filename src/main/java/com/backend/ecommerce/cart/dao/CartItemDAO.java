// src/main/java/com/backend/ecommerce/cart/dao/CartItemDAO.java
package com.backend.ecommerce.cart.dao;

import com.backend.ecommerce.cart.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Object (DAO) interface for CartItem entities.
 * Provides standard CRUD operations for the CartItem entity.
 */
@Repository
public interface CartItemDAO extends JpaRepository<CartItem, Long> { // Cart Item ID is Long

    /**
     * Finds a CartItem within a specific cart for a given product.
     * @param cartId The ID of the cart.
     * @param productId The ID of the product.
     * @return An Optional containing the CartItem if found, or empty otherwise.
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
