package com.backend.ecommerce.products.shop.controller;

import com.backend.ecommerce.products.shop.model.Shop;
import com.backend.ecommerce.products.shop.payload.ShopDTO;
import com.backend.ecommerce.products.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("getall")
    public ResponseEntity<Map<String, Object>> getAllShops() {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching all shops");
        List<ShopDTO> shops = shopService.getAllShops();

        if (shops.isEmpty()) {
            logger.warn("No shops found");
            response.put("success", true); // Still true, just no results
            response.put("message", "No shops found.");
            response.put("shops", shops); // Return empty list
            return ResponseEntity.status(HttpStatus.OK).body(response); // Changed to OK as it's not an error, just no results
        }

        logger.info("Found {} shops", shops.size());
        response.put("success", true);
        response.put("message", "Shops retrieved successfully.");
        response.put("shops", shops);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createShop(@Valid @RequestBody ShopDTO shopDTO) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Creating a new shop with name: {}", shopDTO.getName());

        try {
            ShopDTO createdShop = shopService.createShop(shopDTO);
            logger.info("Shop created successfully with ID: {}", createdShop.getId());
            response.put("success", true);
            response.put("message", "Shop created successfully!");
            response.put("shop", createdShop);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating shop: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Error creating shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            logger.error("Unexpected error during shop creation: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> getShopById(@PathVariable Long shopId) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching shop with ID: {}", shopId);

        return shopService.getShopById(shopId)
                .map(shopDTO -> { // shopDTO is already returned by service
                    logger.info("Shop found: {}", shopDTO.getName());
                    response.put("success", true);
                    response.put("message", "Shop found successfully.");
                    response.put("shop", shopDTO);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Shop with ID: {} not found", shopId);
                    response.put("success", false);
                    response.put("message", "Shop with ID: " + shopId + " not found.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    // Removed the private convertToDTO method as ShopService should return DTOs directly

    @PutMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> updateShop(@PathVariable Long shopId, @Valid @RequestBody ShopDTO shopDTO) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Updating shop with ID: {}", shopId);

        try {
            ShopDTO updatedShop = shopService.updateShop(shopId, shopDTO);
            logger.info("Shop updated successfully with ID: {}", updatedShop.getId());
            response.put("success", true);
            response.put("message", "Shop updated successfully!");
            response.put("shop", updatedShop);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.warn("Shop with ID: {} not found for update: {}", shopId, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to update shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // Assuming RuntimeException implies not found or other client error
        }
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> deleteShop(@PathVariable Long shopId) {
        Map<String, Object> response = new HashMap<>();
        logger.warn("Attempting to delete shop with ID: {}", shopId); // Warn level for delete operations

        try {
            shopService.deleteShop(shopId);
            logger.info("Shop with ID: {} deleted successfully", shopId);
            response.put("success", true);
            response.put("message", "Shop with ID: " + shopId + " deleted successfully.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // 204 No Content is standard for successful delete with no body
        } catch (RuntimeException e) {
            logger.error("Failed to delete shop with ID: {}: {}", shopId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to delete shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response); // Assuming RuntimeException implies not found
        }
    }
}
