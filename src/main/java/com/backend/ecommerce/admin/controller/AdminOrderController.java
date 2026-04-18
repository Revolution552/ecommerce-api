// src/main/java/com/backend/ecommerce/admin/controller/AdminOrderController.java
package com.backend.ecommerce.admin.controller;

import com.backend.ecommerce.order.exception.InvalidOrderStatusTransitionException;
import com.backend.ecommerce.order.exception.OrderNotFoundException;
import com.backend.ecommerce.order.exception.OrderValidationException;
import com.backend.ecommerce.order.model.OrderStatus;
import com.backend.ecommerce.order.model.PaymentStatus;
import com.backend.ecommerce.order.payload.*;
import com.backend.ecommerce.order.service.OrderService;
import com.backend.ecommerce.user.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<OrderResponseDto> orders = orderService.getAllOrders(pageable);
            response.put("success", true);
            response.put("data", orders.getContent());
            response.put("totalElements", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            response.put("currentPage", orders.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching all orders", e);
            response.put("success", false);
            response.put("message", "Error fetching orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchOrders(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<OrderResponseDto> orders = orderService.searchOrders(keyword, pageable);
            response.put("success", true);
            response.put("data", orders.getContent());
            response.put("totalElements", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            response.put("currentPage", orders.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching orders", e);
            response.put("success", false);
            response.put("message", "Error searching orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<OrderResponseDto> orders = orderService.getOrdersByStatus(status, pageable);
            response.put("success", true);
            response.put("data", orders.getContent());
            response.put("totalElements", orders.getTotalElements());
            response.put("totalPages", orders.getTotalPages());
            response.put("currentPage", orders.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching orders by status", e);
            response.put("success", false);
            response.put("message", "Error fetching orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        Map<String, Object> response = new HashMap<>();

        try {
            OrderStatsDto stats = orderService.getOrderStats(start, end);
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching order stats", e);
            response.put("success", false);
            response.put("message", "Error fetching stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Map<String, Object>> getShopOrders(@PathVariable Long shopId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<OrderResponseDto> orders = orderService.getShopOrders(shopId);
            response.put("success", true);
            response.put("data", orders);
            response.put("count", orders.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching shop orders", e);
            response.put("success", false);
            response.put("message", "Error fetching shop orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/shop/{shopId}/stats")
    public ResponseEntity<Map<String, Object>> getShopOrderStats(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        Map<String, Object> response = new HashMap<>();

        try {
            ShopOrderStatsDto stats = orderService.getShopOrderStats(shopId, start, end);
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching shop order stats", e);
            response.put("success", false);
            response.put("message", "Error fetching shop stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<Map<String, Object>> getOrderByNumber(@PathVariable String orderNumber) {
        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.getOrder(orderNumber);
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
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long orderId) {
        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.getOrderById(orderId);
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

    @PutMapping("/{orderNumber}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdateOrderStatusRequest request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.updateOrderStatus(orderNumber, request, adminUser);
            response.put("success", true);
            response.put("message", "Order status updated successfully.");
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (InvalidOrderStatusTransitionException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error updating order status", e);
            response.put("success", false);
            response.put("message", "Error updating order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{orderNumber}/payment")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            OrderResponseDto order = orderService.updatePaymentStatus(orderNumber, request);
            response.put("success", true);
            response.put("message", "Payment status updated successfully.");
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error updating payment status", e);
            response.put("success", false);
            response.put("message", "Error updating payment: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{orderNumber}/cancel")
    public ResponseEntity<Map<String, Object>> cancelOrder(
            @PathVariable String orderNumber,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();
        String reason = request.getOrDefault("reason", "Cancelled by admin");

        try {
            OrderResponseDto order = orderService.cancelOrder(orderNumber, reason, adminUser);
            response.put("success", true);
            response.put("message", "Order cancelled successfully.");
            response.put("data", order);
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

    @PostMapping("/{orderNumber}/refund")
    public ResponseEntity<Map<String, Object>> refundOrder(
            @PathVariable String orderNumber,
            @RequestBody Map<String, Object> refundRequest) {

        Map<String, Object> response = new HashMap<>();

        try {
            UpdatePaymentStatusRequest request = new UpdatePaymentStatusRequest();
            request.setStatus(PaymentStatus.REFUNDED);
            request.setTransactionId((String) refundRequest.get("transactionId"));
            request.setPaymentDetails((String) refundRequest.get("notes"));

            OrderResponseDto order = orderService.updatePaymentStatus(orderNumber, request);
            response.put("success", true);
            response.put("message", "Order refunded successfully.");
            response.put("data", order);
            return ResponseEntity.ok(response);

        } catch (OrderNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error refunding order", e);
            response.put("success", false);
            response.put("message", "Error refunding order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}