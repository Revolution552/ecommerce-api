// src/main/java/com/backend/ecommerce/order/repository/OrderDAO.java
package com.backend.ecommerce.order.dao;

import com.backend.ecommerce.order.model.Order; // Changed import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Data Access Object (DAO) interface for Order models.
 * Extends JpaRepository to provide standard CRUD operations
 * and custom query capabilities for the Order model.
 */
@Repository
public interface OrderDAO extends JpaRepository<Order, Long> {

    /**
     * Finds all orders placed by a specific user.
     * @param userId The ID of the user.
     * @return A list of orders associated with the given user ID.
     */
    List<Order> findByUserId(Long userId);
}
