package com.backend.ecommerce.order.controller;

import com.backend.ecommerce.order.model.OrderStatus;
import com.backend.ecommerce.order.payload.OrderDTO;
import com.backend.ecommerce.order.payload.OrderRequestDTO; // New import
import com.backend.ecommerce.order.service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Order management.
 * Exposes endpoints for creating, retrieving, updating, and deleting orders.
 */
@RestController
@RequestMapping("/api/orders") // Base path for all order-related endpoints
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Creates a new order.
     * POST /api/orders
     *
     * @param orderRequestDTO The OrderRequestDTO containing order and payment details.
     * @return ResponseEntity with a Map containing the created OrderDTO and status.
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        Map<String, Object> response = new HashMap<>();
        // Log the user ID from the nested orderDetails DTO
        logger.info("Attempting to create a new order for user ID: {}", orderRequestDTO.getOrderDetails().getUserId());
        try {
            OrderDTO createdOrder = orderService.createOrder(orderRequestDTO); // Pass the combined DTO
            logger.info("Order created successfully with ID: {}", createdOrder.getId());
            response.put("success", true);
            response.put("message", "Order created successfully!");
            response.put("order", createdOrder); // Include the created order DTO
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating order: Invalid arguments - {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Error creating order: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Unexpected error during order creation: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Retrieves an order by its ID.
     * GET /api/orders/{id}
     *
     * @param id The Long ID of the order to retrieve.
     * @return ResponseEntity with a Map containing the OrderDTO if found, or error message.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) { // Changed from UUID to Long
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching order with ID: {}", id);
        return orderService.getOrderById(id)
                .map(orderDTO -> {
                    logger.info("Order found for ID: {}", id);
                    response.put("success", true);
                    response.put("message", "Order found successfully.");
                    response.put("order", orderDTO);
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    logger.warn("Order with ID: {} not found", id);
                    response.put("success", false);
                    response.put("message", "Order with ID: " + id + " not found.");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    /**
     * Retrieves all orders for a specific user.
     * GET /api/orders/user/{userId}
     *
     * @param userId The ID of the user whose orders are to be retrieved.
     * @return ResponseEntity with a Map containing a list of OrderDTOs or error message.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getOrdersByUserId(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching orders for user ID: {}", userId);
        List<OrderDTO> orders = orderService.getOrdersByUserId(userId);
        if (orders.isEmpty()) {
            logger.warn("No orders found for user ID: {}", userId);
            response.put("success", true); // Still true, just no results
            response.put("message", "No orders found for user ID: " + userId + ".");
            response.put("orders", orders); // Return empty list
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        logger.info("Found {} orders for user ID: {}", orders.size(), userId);
        response.put("success", true);
        response.put("message", "Orders found successfully for user ID: " + userId + ".");
        response.put("orders", orders);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Updates the status of an existing order.
     * PUT /api/orders/{id}/status
     *
     * @param id The Long ID of the order to update.
     * @param newStatus The new status for the order (e.g., "SHIPPED", "DELIVERED").
     * @return ResponseEntity with a Map containing the updated OrderDTO or error message.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id, @RequestParam OrderStatus newStatus) { // Changed from UUID to Long
        Map<String, Object> response = new HashMap<>();
        logger.info("Attempting to update status for order ID: {} to {}", id, newStatus);
        try {
            OrderDTO updatedOrder = orderService.updateOrderStatus(id, newStatus);
            logger.info("Order ID: {} status updated successfully to {}", id, newStatus);
            response.put("success", true);
            response.put("message", "Order status updated successfully.");
            response.put("order", updatedOrder);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warn("Order with ID: {} not found for status update", id);
            response.put("success", false);
            response.put("message", "Order with ID: " + id + " not found.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Unexpected error updating status for order ID: {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Deletes an order by its ID.
     * DELETE /api/orders/{id}
     *
     * @param id The Long ID of the order to delete.
     * @return ResponseEntity with a Map containing success/failure message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOrder(@PathVariable Long id) { // Changed from UUID to Long
        Map<String, Object> response = new HashMap<>();
        logger.warn("Attempting to delete order with ID: {}", id);
        try {
            orderService.deleteOrder(id);
            logger.info("Order with ID: {} deleted successfully", id);
            response.put("success", true);
            response.put("message", "Order with ID: " + id + " deleted successfully.");
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT); // 204 No Content is standard for successful delete with no body
        } catch (EntityNotFoundException e) {
            logger.warn("Order with ID: {} not found for deletion", id);
            response.put("success", false);
            response.put("message", "Order with ID: " + id + " not found for deletion.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Unexpected error deleting order with ID: {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}