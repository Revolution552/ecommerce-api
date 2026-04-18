// src/main/java/com/backend/ecommerce/order/controller/OrderController.java
package com.backend.ecommerce.order.controller;

import com.backend.ecommerce.order.exception.InsufficientStockException;
import com.backend.ecommerce.order.exception.InvalidOrderStatusTransitionException;
import com.backend.ecommerce.order.exception.OrderNotFoundException;
import com.backend.ecommerce.order.exception.OrderValidationException;
import com.backend.ecommerce.order.model.OrderStatus;
import com.backend.ecommerce.order.payload.*;
import com.backend.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.createOrder(userDetails.getUsername(), request);
            response.put("success", true);
            response.put("message", "Order created successfully.");
            response.put("data", order);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (OrderValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (InsufficientStockException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            logger.error("Error creating order", e);
            response.put("success", false);
            response.put("message", "Error creating order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/from-cart")
    public ResponseEntity<Map<String, Object>> createOrderFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateOrderFromCartRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.createOrderFromCart(userDetails.getUsername(), request);
            response.put("success", true);
            response.put("message", "Order created successfully from cart.");
            response.put("data", order);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (OrderValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (InsufficientStockException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            logger.error("Error creating order from cart", e);
            response.put("success", false);
            response.put("message", "Error creating order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<OrderSummaryDto> orders = orderService.getUserOrders(userDetails.getUsername(), pageable);
            response.put("success", true);
            response.put("data", orders.getContent());
            response.put("totalElements", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            response.put("currentPage", orders.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching user orders", e);
            response.put("success", false);
            response.put("message", "Error fetching orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getUserOrdersByStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable OrderStatus status) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<OrderSummaryDto> orders = orderService.getUserOrdersByStatus(userDetails.getUsername(), status);
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching user orders by status", e);
            response.put("success", false);
            response.put("message", "Error fetching orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByNumber(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber) {

        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.getOrder(orderNumber);

            // Verify order belongs to user (admin check handled separately)
            if (!order.userEmail().equals(userDetails.getUsername())) {
                response.put("success", false);
                response.put("message", "Access denied to this order");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.put("success", true);
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error fetching order", e);
            response.put("success", false);
            response.put("message", "Error fetching order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.getOrderById(orderId);

            // Verify order belongs to user
            if (!order.userEmail().equals(userDetails.getUsername())) {
                response.put("success", false);
                response.put("message", "Access denied to this order");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.put("success", true);
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error fetching order", e);
            response.put("success", false);
            response.put("message", "Error fetching order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String orderNumber,
            @RequestBody(required = false) Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();
        String reason = request != null ? request.getOrDefault("reason", "Customer requested cancellation") : "Customer requested cancellation";

        try {
            // Verify order belongs to user
            OrderResponseDto order = orderService.getOrder(orderNumber);
            if (!order.userEmail().equals(userDetails.getUsername())) {
                response.put("success", false);
                response.put("message", "Access denied to this order");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            OrderResponseDto cancelledOrder = orderService.cancelOrder(orderNumber, reason, null);
            response.put("success", true);
            response.put("message", "Order cancelled successfully.");
            response.put("data", cancelledOrder);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (OrderValidationException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error cancelling order", e);
            response.put("success", false);
            response.put("message", "Error cancelling order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/track/{orderNumber}")
    public ResponseEntity<Map<String, Object>> trackOrder(@PathVariable String orderNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.getOrder(orderNumber);

            // Only return tracking info, not full order details
            Map<String, Object> trackingInfo = new HashMap<>();
            trackingInfo.put("orderNumber", order.orderNumber());
            trackingInfo.put("status", order.status());
            trackingInfo.put("fulfillmentStatus", order.fulfillmentStatus());
            trackingInfo.put("createdAt", order.createdAt());
            trackingInfo.put("estimatedDelivery", order.shipping() != null ? order.shipping().estimatedDelivery() : null);
            trackingInfo.put("trackingNumber", order.shipping() != null ? order.shipping().trackingNumber() : null);
            trackingInfo.put("trackingUrl", order.shipping() != null ? order.shipping().trackingUrl() : null);
            trackingInfo.put("carrier", order.shipping() != null ? order.shipping().carrier() : null);

            response.put("success", true);
            response.put("data", trackingInfo);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error tracking order", e);
            response.put("success", false);
            response.put("message", "Error tracking order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}