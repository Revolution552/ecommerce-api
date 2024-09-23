package com.backend.ecommerce.orders.controller;

import com.backend.ecommerce.orders.model.Order;
import com.backend.ecommerce.orders.model.OrderStatus;
import com.backend.ecommerce.orders.payload.OrderDTO;
import com.backend.ecommerce.orders.payload.OrderItemDTO;
import com.backend.ecommerce.orders.service.OrderService;
import com.backend.ecommerce.users.model.LocalUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create an order
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal LocalUser user,
                                         @Valid @RequestBody OrderDTO orderRequest) {
        if (user == null) {
            logger.warn("Unauthorized attempt to create an order.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        // Check if items are present in the orderRequest
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            logger.warn("Order request contains no items.");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Order must contain at least one item"));
        }

        // Validate each OrderItemDTO
        for (OrderItemDTO item : orderRequest.getItems()) {
            if (item.getQuantity() <= 0) {
                logger.warn("Invalid quantity for product ID {}: {}", item.getProductId(), item.getQuantity());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quantity must be greater than zero for product ID: " + item.getProductId()));
            }
            if (item.getPrice() < 0) {
                logger.warn("Invalid price for product ID {}: {}", item.getProductId(), item.getPrice());
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Price must be a non-negative value for product ID: " + item.getProductId()));
            }
        }

        // Extract product IDs and quantities from the orderRequest
        List<Long> productIds = orderRequest.getItems().stream()
                .map(OrderItemDTO::getProductId)
                .toList();

        List<Integer> quantities = orderRequest.getItems().stream()
                .map(OrderItemDTO::getQuantity)
                .toList();

        // Create the order using the service
        Order createdOrder = orderService.createOrder(productIds, quantities, user);
        logger.info("Order created successfully: {}", createdOrder.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Order created successfully", "order", createdOrder));
    }

    // Get orders for logged-in user
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(@AuthenticationPrincipal LocalUser user) {
        if (user == null) {
            logger.warn("Unauthorized attempt to get user orders.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        List<Order> orders = orderService.getOrdersByUser(user);
        logger.info("Fetched orders for user: {}", user.getId());
        return ResponseEntity.ok(Map.of("orders", orders));
    }

    // Get a specific order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal LocalUser user,
                                      @PathVariable Long orderId) {
        if (user == null) {
            logger.warn("Unauthorized attempt to get order with ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        try {
            Order order = orderService.getOrderById(orderId, user);
            logger.info("Fetched order: {}", orderId);
            return ResponseEntity.ok(Map.of("order", order));
        } catch (IllegalArgumentException e) {
            logger.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Delete an order by ID
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@AuthenticationPrincipal LocalUser user,
                                         @PathVariable Long orderId) {
        if (user == null) {
            logger.warn("Unauthorized attempt to delete order with ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        boolean isDeleted = orderService.deleteOrder(orderId, user);
        if (isDeleted) {
            logger.info("Order deleted successfully: {}", orderId);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "Order deleted successfully"));
        } else {
            logger.warn("Attempt to delete order failed: {}", orderId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied or order not found"));
        }
    }

    // Update the status of an order
    public static class StatusUpdateRequest {
        private OrderStatus status;

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@AuthenticationPrincipal LocalUser user,
                                               @PathVariable Long orderId,
                                               @Valid @RequestBody StatusUpdateRequest request) {
        if (user == null) {
            logger.warn("Unauthorized attempt to update order status for order ID: {}", orderId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus(), user);
            logger.info("Updated order status for order ID: {}", orderId);
            return ResponseEntity.ok(Map.of("message", "Order status updated", "order", updatedOrder));
        } catch (IllegalArgumentException e) {
            logger.error("Error updating order status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
