// src/main/java/com/backend/ecommerce/shop/controller/ShopController.java
package com.backend.ecommerce.shop.controller;

import com.backend.ecommerce.shop.exception.ShopNotFoundException;
import com.backend.ecommerce.shop.payload.ShopResponseDto;
import com.backend.ecommerce.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllShops(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ShopResponseDto> shops = shopService.getAllShopsPaginated(pageable);
            response.put("success", true);
            response.put("data", shops.getContent());
            response.put("totalElements", shops.getTotalElements());
            response.put("totalPages", shops.getTotalPages());
            response.put("currentPage", shops.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching shops", e);
            response.put("success", false);
            response.put("message", "Error fetching shops: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedShops() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ShopResponseDto> featuredShops = shopService.getFeaturedShops();
            response.put("success", true);
            response.put("data", featuredShops);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching featured shops", e);
            response.put("success", false);
            response.put("message", "Error fetching featured shops: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchShops(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ShopResponseDto> shops = shopService.searchShops(keyword, pageable);
            response.put("success", true);
            response.put("data", shops.getContent());
            response.put("totalElements", shops.getTotalElements());
            response.put("totalPages", shops.getTotalPages());
            response.put("currentPage", shops.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching shops", e);
            response.put("success", false);
            response.put("message", "Error searching shops: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> getShopBySlug(@PathVariable String slug) {
        Map<String, Object> response = new HashMap<>();

        try {
            ShopResponseDto shop = shopService.getShopBySlug(slug);
            response.put("success", true);
            response.put("data", shop);
            return ResponseEntity.ok(response);

        } catch (ShopNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> getShopById(@PathVariable Long shopId) {
        Map<String, Object> response = new HashMap<>();

        try {
            ShopResponseDto shop = shopService.getShopById(shopId);
            response.put("success", true);
            response.put("data", shop);
            return ResponseEntity.ok(response);

        } catch (ShopNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}