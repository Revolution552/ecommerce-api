package com.backend.ecommerce.orders.service;

import com.backend.ecommerce.orders.model.Order;
import com.backend.ecommerce.orders.model.OrderStatus;
import com.backend.ecommerce.orders.model.dao.OrderDAO;
import com.backend.ecommerce.users.model.LocalUser;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderDAO orderDAO;

    @Autowired
    public OrderService(OrderDAO orderDAO) {
        this.orderDAO = orderDAO;
    }

    // Create an order for a specific user
    public Order createOrder(Order order, LocalUser user) {
        order.setUser(user);
        return orderDAO.save(order);
    }

    // Get all orders for a specific user
    public List<Order> getOrdersByUser(LocalUser user) {
        return orderDAO.findByUserId(user.getId());
    }

    // Find order by ID with items initialized to avoid LazyInitializationException
    @Transactional
    public Optional<Order> findOrderById(Long orderId) {
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        orderOpt.ifPresent(order -> Hibernate.initialize(order.getItems())); // Initialize lazy-loaded collection
        return orderOpt;
    }

    // Update order status
    @Transactional
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus, LocalUser user) {
        Optional<Order> orderOpt = orderDAO.findById(orderId);

        if (orderOpt.isPresent() && orderOpt.get().getUser().getId().equals(user.getId())) {
            Order order = orderOpt.get();
            order.setStatus(newStatus); // Update the order status
            return orderDAO.save(order); // Save the updated order
        } else {
            throw new IllegalArgumentException("Access denied or order not found");
        }
    }

    // Get order details by order ID with items initialized
    @Transactional
    public Order getOrderById(Long orderId, LocalUser user) {
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        if (orderOpt.isPresent() && orderOpt.get().getUser().getId().equals(user.getId())) {
            Order order = orderOpt.get();
            Hibernate.initialize(order.getItems());  // Initialize lazy-loaded collection
            return order;
        } else {
            throw new IllegalArgumentException("Access denied or order not found");
        }
    }

    // Delete an order by order ID
    @Transactional
    public boolean deleteOrder(Long orderId, LocalUser user) {
        Optional<Order> orderOpt = orderDAO.findById(orderId);
        if (orderOpt.isPresent() && orderOpt.get().getUser().getId().equals(user.getId())) {
            orderDAO.deleteById(orderId);
            return true;
        } else {
            return false;
        }
    }

}
