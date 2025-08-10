// src/main/java/com/backend/ecommerce/cart/controller/CartController.java
package com.backend.ecommerce.cart.controller;

import com.backend.ecommerce.cart.payload.CartDTO;
import com.backend.ecommerce.cart.payload.CartItemDTO;
import com.backend.ecommerce.cart.service.CartService;
import com.backend.ecommerce.user.model.User; // Assuming User model is available for @AuthenticationPrincipal
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Shopping Cart management.
 * Exposes endpoints for viewing, adding, updating, and removing items from a user's cart.
 */
@RestController
@RequestMapping("/api/cart") // Base path for all cart-related endpoints
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Retrieves the authenticated user's shopping cart.
     * GET /api/cart
     * @param user The authenticated user.
     * @return ResponseEntity with the CartDTO and HTTP status 200 (OK).
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to get cart (no authenticated user).");
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.info("Fetching cart for user ID: {}", user.getId());
        try {
            CartDTO cartDTO = cartService.getOrCreateCart(user.getId());
            logger.info("Cart retrieved/created successfully for user ID: {}. Total amount: {}", user.getId(), cartDTO.getTotalAmount());
            response.put("success", true);
            response.put("message", "Cart retrieved successfully.");
            response.put("cart", cartDTO);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error fetching cart for user ID {}: {}", user.getId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Adds a product to the cart or updates its quantity.
     * POST /api/cart/items
     * @param user The authenticated user.
     * @param cartItemDTO The CartItemDTO containing productId and quantity.
     * @return ResponseEntity with the updated CartDTO and HTTP status 200 (OK).
     */
    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addOrUpdateCartItem(@AuthenticationPrincipal User user,
                                                                   @Valid @RequestBody CartItemDTO cartItemDTO) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to add/update cart item (no authenticated user).");
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.info("User {} attempting to add/update product {} with quantity {} in cart.",
                user.getId(), cartItemDTO.getProductId(), cartItemDTO.getQuantity());
        try {
            CartDTO updatedCart = cartService.addOrUpdateCartItem(user.getId(), cartItemDTO);
            logger.info("Product {} added/updated successfully in cart for user {}. New total amount: {}", cartItemDTO.getProductId(), user.getId(), updatedCart.getTotalAmount());
            response.put("success", true);
            response.put("message", "Cart item added/updated successfully.");
            response.put("cart", updatedCart);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warn("Product not found for cart operation: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Product not found: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid quantity for cart item: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Invalid quantity: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Error adding/updating cart item for user {}: {}", user.getId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Updates the quantity of a specific product in the cart.
     * PUT /api/cart/items/{productId}?quantity={newQuantity}
     * @param user The authenticated user.
     * @param productId The ID of the product to update.
     * @param newQuantity The new quantity for the product.
     * @return ResponseEntity with the updated CartDTO and HTTP status 200 (OK).
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<Map<String, Object>> updateCartItemQuantity(@AuthenticationPrincipal User user,
                                                                      @PathVariable Long productId,
                                                                      @RequestParam Integer newQuantity) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to update cart item quantity (no authenticated user).");
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.info("User {} attempting to update quantity of product {} to {} in cart.",
                user.getId(), productId, newQuantity);
        try {
            CartDTO updatedCart = cartService.updateCartItemQuantity(user.getId(), productId, newQuantity);
            logger.info("Quantity of product {} updated to {} for user {}. New total amount: {}", productId, newQuantity, user.getId(), updatedCart.getTotalAmount());
            response.put("success", true);
            response.put("message", "Cart item quantity updated successfully.");
            response.put("cart", updatedCart);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (EntityNotFoundException e) {
            logger.warn("Cart or product {} not found for quantity update: {}", productId, e.getMessage());
            response.put("success", false);
            response.put("message", "Cart or product not found: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid new quantity for cart item: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Invalid quantity: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            logger.error("Error updating cart item quantity for user {}: {}", user.getId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Removes a specific product from the cart.
     * DELETE /api/cart/items/{productId}
     * @param user The authenticated user.
     * @param productId The ID of the product to remove.
     * @return ResponseEntity with success message and HTTP status 200 (OK) or 204 (No Content).
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Map<String, Object>> removeCartItem(@AuthenticationPrincipal User user,
                                                              @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to remove cart item (no authenticated user).");
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.warn("User {} attempting to remove product {} from cart.", user.getId(), productId); // Warn for delete
        try {
            CartDTO updatedCart = cartService.removeCartItem(user.getId(), productId);
            logger.info("Product {} removed from cart for user {}. New total amount: {}", productId, user.getId(), updatedCart.getTotalAmount());
            response.put("success", true);
            response.put("message", "Product removed from cart successfully.");
            response.put("cart", updatedCart); // Return updated cart
            return new ResponseEntity<>(response, HttpStatus.OK); // Changed to OK with body
        } catch (EntityNotFoundException e) {
            logger.warn("Cart or product {} not found for removal: {}", productId, e.getMessage());
            response.put("success", false);
            response.put("message", "Cart or product not found: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Error removing cart item for user {}: {}", user.getId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Clears all items from the authenticated user's cart.
     * DELETE /api/cart/clear
     * @param user The authenticated user.
     * @return ResponseEntity with success message and HTTP status 200 (OK) or 204 (No Content).
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to clear cart (no authenticated user).");
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.warn("User {} attempting to clear their cart.", user.getId()); // Warn for clear operation
        try {
            CartDTO clearedCart = cartService.clearCart(user.getId());
            logger.info("Cart cleared successfully for user {}. New total amount: {}", user.getId(), clearedCart.getTotalAmount());
            response.put("success", true);
            response.put("message", "Cart cleared successfully.");
            response.put("cart", clearedCart); // Return empty cart
            return new ResponseEntity<>(response, HttpStatus.OK); // Changed to OK with body
        } catch (EntityNotFoundException e) {
            logger.warn("Cart not found for user {} during clear operation: {}", user.getId(), e.getMessage());
            response.put("success", false);
            response.put("message", "Cart not found: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            logger.error("Error clearing cart for user {}: {}", user.getId(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}