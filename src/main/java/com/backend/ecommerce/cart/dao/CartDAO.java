// src/main/java/com/backend/ecommerce/cart/dao/CartDAO.java
package com.backend.ecommerce.cart.dao;

import com.backend.ecommerce.cart.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access Object (DAO) interface for Cart entities.
 * Provides standard CRUD operations and custom query capabilities for the Cart entity.
 */
@Repository
public interface CartDAO extends JpaRepository<Cart, Long> { // Cart ID is Long

    /**
     * Finds a cart by the associated user ID.
     * @param userId The ID of the user.
     * @return An Optional containing the Cart if found, or empty otherwise.
     */
    Optional<Cart> findByUserId(Long userId);
}
