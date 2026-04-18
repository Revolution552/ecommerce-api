// src/main/java/com/backend/ecommerce/favorites/controller/FavoriteController.java
package com.backend.ecommerce.favorites.controller;

import com.backend.ecommerce.favorites.exception.FavoriteAlreadyExistsException;
import com.backend.ecommerce.favorites.exception.FavoriteNotFoundException;
import com.backend.ecommerce.favorites.payload.*;
import com.backend.ecommerce.favorites.service.FavoriteService;
import com.backend.ecommerce.product.exception.ProductNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/favorites")
public class FavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteController.class);

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addToFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody FavoriteCreateRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            FavoriteResponseDto favorite = favoriteService.addToFavorites(userDetails.getUsername(), request);
            response.put("success", true);
            response.put("message", "Product added to favorites.");
            response.put("data", favorite);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (FavoriteAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error adding to favorites", e);
            response.put("success", false);
            response.put("message", "Error adding to favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkAddToFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BulkFavoriteRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<FavoriteResponseDto> favorites = favoriteService.bulkAddToFavorites(userDetails.getUsername(), request);
            response.put("success", true);
            response.put("message", "Products added to favorites.");
            response.put("data", favorites);
            response.put("count", favorites.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error bulk adding to favorites", e);
            response.put("success", false);
            response.put("message", "Error adding to favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            FavoriteListResponseDto favorites = favoriteService.getUserFavorites(userDetails.getUsername(), pageable);
            response.put("success", true);
            response.put("data", favorites);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching favorites", e);
            response.put("success", false);
            response.put("message", "Error fetching favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllUserFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<FavoriteResponseDto> favorites = favoriteService.getAllUserFavorites(userDetails.getUsername());
            response.put("success", true);
            response.put("data", favorites);
            response.put("count", favorites.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching all favorites", e);
            response.put("success", false);
            response.put("message", "Error fetching favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            FavoriteListResponseDto favorites = favoriteService.searchUserFavorites(
                    userDetails.getUsername(), keyword, pageable);
            response.put("success", true);
            response.put("data", favorites);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching favorites", e);
            response.put("success", false);
            response.put("message", "Error searching favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Object>> checkFavoriteStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {

        Map<String, Object> response = new HashMap<>();

        try {
            FavoriteStatusResponseDto status = favoriteService.checkFavoriteStatus(userDetails.getUsername(), productId);
            response.put("success", true);
            response.put("data", status);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking favorite status", e);
            response.put("success", false);
            response.put("message", "Error checking favorite status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/check/bulk")
    public ResponseEntity<Map<String, Object>> checkBulkFavoriteStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody List<Long> productIds) {

        Map<String, Object> response = new HashMap<>();

        try {
            Map<Long, Boolean> status = favoriteService.checkBulkFavoriteStatus(userDetails.getUsername(), productIds);
            response.put("success", true);
            response.put("data", status);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error checking bulk favorite status", e);
            response.put("success", false);
            response.put("message", "Error checking favorite status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getFavoriteCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            Long count = favoriteService.getFavoriteCount(userDetails.getUsername());
            response.put("success", true);
            response.put("count", count);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting favorite count", e);
            response.put("success", false);
            response.put("message", "Error getting favorite count: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateFavorite(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody FavoriteUpdateRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            FavoriteResponseDto favorite = favoriteService.updateFavorite(
                    userDetails.getUsername(), productId, request);
            response.put("success", true);
            response.put("message", "Favorite updated.");
            response.put("data", favorite);
            return ResponseEntity.ok(response);

        } catch (FavoriteNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error updating favorite", e);
            response.put("success", false);
            response.put("message", "Error updating favorite: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> removeFromFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {

        Map<String, Object> response = new HashMap<>();

        try {
            favoriteService.removeFromFavorites(userDetails.getUsername(), productId);
            response.put("success", true);
            response.put("message", "Product removed from favorites.");
            return ResponseEntity.ok(response);

        } catch (FavoriteNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error removing from favorites", e);
            response.put("success", false);
            response.put("message", "Error removing from favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkRemoveFromFavorites(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BulkFavoriteRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            favoriteService.bulkRemoveFromFavorites(userDetails.getUsername(), request);
            response.put("success", true);
            response.put("message", "Products removed from favorites.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error bulk removing from favorites", e);
            response.put("success", false);
            response.put("message", "Error removing from favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearAllFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            favoriteService.clearAllFavorites(userDetails.getUsername());
            response.put("success", true);
            response.put("message", "All favorites cleared.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error clearing favorites", e);
            response.put("success", false);
            response.put("message", "Error clearing favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/move-to-cart")
    public ResponseEntity<Map<String, Object>> moveAllToCart(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            favoriteService.moveAllToCart(userDetails.getUsername());
            response.put("success", true);
            response.put("message", "All favorites moved to cart.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error moving favorites to cart", e);
            response.put("success", false);
            response.put("message", "Error moving favorites to cart: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}