// src/main/java/com/backend/ecommerce/cart/controller/CartController.java
package com.backend.ecommerce.cart.controller;

import com.backend.ecommerce.cart.exception.CartItemNotFoundException;
import com.backend.ecommerce.cart.exception.CartNotFoundException;
import com.backend.ecommerce.cart.exception.InvalidQuantityException;
import com.backend.ecommerce.cart.payload.*;
import com.backend.ecommerce.cart.service.CartService;
import com.backend.ecommerce.product.exception.ProductNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.getCart(userDetails.getUsername());
            response.put("success", true);
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (CartNotFoundException e) {
            response.put("success", true);
            response.put("data", null);
            response.put("message", "Cart is empty");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching cart", e);
            response.put("success", false);
            response.put("message", "Error fetching cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getCartSummary(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartSummaryDto summary = cartService.getCartSummary(userDetails.getUsername());
            response.put("success", true);
            response.put("data", summary);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching cart summary", e);
            response.put("success", false);
            response.put("message", "Error fetching cart summary: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartValidationResultDto validation = cartService.validateCart(userDetails.getUsername());
            response.put("success", true);
            response.put("data", validation);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error validating cart", e);
            response.put("success", false);
            response.put("message", "Error validating cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.addToCart(userDetails.getUsername(), request);
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
            logger.error("Error adding to cart", e);
            response.put("success", false);
            response.put("message", "Error adding to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/items/bulk")
    public ResponseEntity<Map<String, Object>> bulkAddToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BulkAddToCartRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.bulkAddToCart(userDetails.getUsername(), request);
            response.put("success", true);
            response.put("message", "Items added to cart.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error bulk adding to cart", e);
            response.put("success", false);
            response.put("message", "Error adding items to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/items/{productId}")
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.updateCartItem(userDetails.getUsername(), productId, request);
            response.put("success", true);
            response.put("message", "Cart item updated.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (CartItemNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (InvalidQuantityException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error updating cart item", e);
            response.put("success", false);
            response.put("message", "Error updating cart item: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.removeFromCart(userDetails.getUsername(), productId);
            response.put("success", true);
            response.put("message", "Item removed from cart.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error removing from cart", e);
            response.put("success", false);
            response.put("message", "Error removing from cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/items/bulk")
    public ResponseEntity<Map<String, Object>> bulkRemoveFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<Long> productIds) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.bulkRemoveFromCart(userDetails.getUsername(), productIds);
            response.put("success", true);
            response.put("message", "Items removed from cart.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error bulk removing from cart", e);
            response.put("success", false);
            response.put("message", "Error removing items from cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            cartService.clearCart(userDetails.getUsername());
            response.put("success", true);
            response.put("message", "Cart cleared.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error clearing cart", e);
            response.put("success", false);
            response.put("message", "Error clearing cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/items/{productId}/select")
    public ResponseEntity<Map<String, Object>> updateItemSelection(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam Boolean selected) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.updateSelection(userDetails.getUsername(), productId, selected);
            response.put("success", true);
            response.put("message", "Item selection updated.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (CartItemNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error updating item selection", e);
            response.put("success", false);
            response.put("message", "Error updating selection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/select-all")
    public ResponseEntity<Map<String, Object>> selectAllItems(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Boolean selected) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.selectAll(userDetails.getUsername(), selected);
            response.put("success", true);
            response.put("message", "All items selection updated.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating all items selection", e);
            response.put("success", false);
            response.put("message", "Error updating selection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/shop/{shopId}/select")
    public ResponseEntity<Map<String, Object>> selectShopItems(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long shopId,
            @RequestParam Boolean selected) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.selectByShop(userDetails.getUsername(), shopId, selected);
            response.put("success", true);
            response.put("message", "Shop items selection updated.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating shop items selection", e);
            response.put("success", false);
            response.put("message", "Error updating selection: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<Map<String, Object>> mergeGuestCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String sessionId) {

        Map<String, Object> response = new HashMap<>();

        try {
            CartResponseDto cart = cartService.mergeGuestCart(userDetails.getUsername(), sessionId);
            response.put("success", true);
            response.put("message", "Guest cart merged successfully.");
            response.put("data", cart);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error merging guest cart", e);
            response.put("success", false);
            response.put("message", "Error merging cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}