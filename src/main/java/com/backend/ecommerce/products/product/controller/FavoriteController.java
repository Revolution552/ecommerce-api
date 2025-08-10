package com.backend.ecommerce.products.product.controller;

import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.service.FavoriteService;
import com.backend.ecommerce.user.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> addProductToFavorites(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to add product {} to favorites (no authenticated user).", productId);
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.info("User {} attempting to add product {} to favorites.", user.getUsername(), productId);
        try {
            favoriteService.addProductToFavorites(user, productId);
            logger.info("Product {} added to favorites for user {}.", productId, user.getUsername());
            response.put("success", true);
            response.put("message", "Product added to favorites.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Error adding product {} to favorites for user {}: {}", productId, user.getUsername(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to add product to favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response); // Use BAD_REQUEST or INTERNAL_SERVER_ERROR based on specific error
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> removeProductFromFavorites(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to remove product {} from favorites (no authenticated user).", productId);
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.info("User {} attempting to remove product {} from favorites.", user.getUsername(), productId);
        try {
            favoriteService.removeProductFromFavorites(user, productId);
            logger.info("Product {} removed from favorites for user {}.", productId, user.getUsername());
            response.put("success", true);
            response.put("message", "Product removed from favorites.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            logger.error("Error removing product {} from favorites for user {}: {}", productId, user.getUsername(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to remove product from favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserFavorites(@AuthenticationPrincipal User user) {
        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to get favorites (no authenticated user).");
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        logger.info("Fetching favorites for user: {}", user.getUsername());
        try {
            List<Product> favorites = favoriteService.getUserFavorites(user);
            logger.info("Found {} favorite products for user {}.", favorites.size(), user.getUsername());
            response.put("success", true);
            response.put("message", "User favorites retrieved successfully.");
            response.put("favorites", favorites);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error fetching favorites for user {}: {}", user.getUsername(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
