package com.backend.ecommerce.orders.model.dao;

import com.backend.ecommerce.orders.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderDAO extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);
}
