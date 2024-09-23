package com.backend.ecommerce.orders.service;

import com.backend.ecommerce.orders.model.Order;
import com.backend.ecommerce.orders.model.OrderItem;
import com.backend.ecommerce.orders.model.OrderStatus;
import com.backend.ecommerce.orders.model.dao.OrderDAO;
import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.dao.ProductDAO; // Import the ProductDAO
import com.backend.ecommerce.products.product.model.ProductNotFoundException;
import com.backend.ecommerce.users.model.LocalUser;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderDAO orderDAO;
    private final ProductDAO productDAO; // Declare ProductDAO

    @Autowired
    public OrderService(OrderDAO orderDAO, ProductDAO productDAO) {
        this.orderDAO = orderDAO;
        this.productDAO = productDAO; // Initialize ProductDAO
    }

    // Create an order for a specific user
    public Order createOrder(List<Long> productIds, List<Integer> quantities, LocalUser user) {
        if (productIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Product IDs and quantities must have the same size");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(new Date());

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0.0;

        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Product product = productDAO.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + productId)); // Custom exception

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(productId); // Set the entire product object
            orderItem.setQuantity(quantities.get(i));
            orderItem.setPrice(product.getPrice()); // Assuming Product has a getPrice method
            orderItem.setOrder(order);

            totalAmount += orderItem.getPrice() * orderItem.getQuantity(); // Calculate total amount
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        order.setTotalAmount(totalAmount); // Set the calculated total amount

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
