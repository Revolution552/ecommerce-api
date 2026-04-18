// src/main/java/com/backend/ecommerce/admin/controller/AdminFavoriteController.java
package com.backend.ecommerce.admin.controller;

import com.backend.ecommerce.favorites.payload.FavoriteResponseDto;
import com.backend.ecommerce.favorites.service.FavoriteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/favorites")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFavoriteController {

    private static final Logger logger = LoggerFactory.getLogger(AdminFavoriteController.class);

    private final FavoriteService favoriteService;

    public AdminFavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFavorites(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<FavoriteResponseDto> favorites = favoriteService.getAllFavorites(pageable);
            response.put("success", true);
            response.put("data", favorites.getContent());
            response.put("totalElements", favorites.getTotalElements());
            response.put("totalPages", favorites.getTotalPages());
            response.put("currentPage", favorites.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching all favorites", e);
            response.put("success", false);
            response.put("message", "Error fetching favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserFavorites(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<FavoriteResponseDto> favorites = favoriteService.getUserFavoritesByUserId(userId);
            response.put("success", true);
            response.put("data", favorites);
            response.put("count", favorites.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching user favorites", e);
            response.put("success", false);
            response.put("message", "Error fetching favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> deleteFavoritesByProduct(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            favoriteService.deleteFavoritesByProductId(productId);
            response.put("success", true);
            response.put("message", "All favorites for product deleted.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting favorites by product", e);
            response.put("success", false);
            response.put("message", "Error deleting favorites: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}