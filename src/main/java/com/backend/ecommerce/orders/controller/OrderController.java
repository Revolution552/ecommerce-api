package com.backend.ecommerce.orders.controller;

import com.backend.ecommerce.orders.model.Order;
import com.backend.ecommerce.orders.model.OrderStatus;
import com.backend.ecommerce.orders.service.OrderService;
import com.backend.ecommerce.users.model.LocalUser;
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

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Create an order
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@AuthenticationPrincipal LocalUser user,
                                         @Valid @RequestBody Order orderRequest) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        Order createdOrder = orderService.createOrder(orderRequest, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Order created successfully", "order", createdOrder));
    }

    // Get orders for logged-in user
    @GetMapping("/user")
    public ResponseEntity<?> getUserOrders(@AuthenticationPrincipal LocalUser user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        List<Order> orders = orderService.getOrdersByUser(user);
        return ResponseEntity.ok(Map.of("orders", orders));
    }

    // Get a specific order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@AuthenticationPrincipal LocalUser user,
                                      @PathVariable Long orderId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        try {
            Order order = orderService.getOrderById(orderId, user);
            return ResponseEntity.ok(Map.of("order", order));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Delete an order by ID
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@AuthenticationPrincipal LocalUser user,
                                         @PathVariable Long orderId) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        boolean isDeleted = orderService.deleteOrder(orderId, user);
        if (isDeleted) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(Map.of("message", "Order deleted successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied or order not found"));
        }
    }

    // Update the status of an order
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@AuthenticationPrincipal LocalUser user,
                                               @PathVariable Long orderId,
                                               @RequestParam("status") OrderStatus newStatus) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "User not authenticated"));
        }

        try {
            Order updatedOrder = orderService.updateOrderStatus(orderId, newStatus, user);
            return ResponseEntity.ok(Map.of("message", "Order status updated", "order", updatedOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
