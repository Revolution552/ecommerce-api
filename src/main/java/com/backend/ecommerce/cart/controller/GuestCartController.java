// src/main/java/com/backend/ecommerce/cart/controller/GuestCartController.java
package com.backend.ecommerce.cart.controller;

import com.backend.ecommerce.cart.exception.InvalidQuantityException;
import com.backend.ecommerce.cart.payload.AddToCartRequest;
import com.backend.ecommerce.cart.payload.CartResponseDto;
import com.backend.ecommerce.cart.service.CartService;
import com.backend.ecommerce.product.exception.ProductNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/guest/cart")
public class GuestCartController {

    private static final Logger logger = LoggerFactory.getLogger(GuestCartController.class);

    private final CartService cartService;

    public GuestCartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> getSessionId() {
        Map<String, Object> response = new HashMap<>();
        String sessionId = UUID.randomUUID().toString();

        response.put("success", true);
        response.put("sessionId", sessionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getGuestCart(@RequestParam String sessionId) {
        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.getCartBySession(sessionId);
            response.put("success", true);
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching guest cart", e);
            response.put("success", false);
            response.put("message", "Error fetching cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToGuestCart(
            @RequestParam String sessionId,
            @Valid @RequestBody AddToCartRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.addToGuestCart(sessionId, request);
            response.put("success", true);
            response.put("message", "Item added to cart.");
            response.put("data", cart);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (InvalidQuantityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error adding to guest cart", e);
            response.put("success", false);
            response.put("message", "Error adding to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}