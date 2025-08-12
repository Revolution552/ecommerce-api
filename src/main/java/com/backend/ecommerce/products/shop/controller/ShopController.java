package com.backend.ecommerce.products.shop.controller;

import com.backend.ecommerce.products.shop.payload.ShopDTO;
import com.backend.ecommerce.products.shop.service.ShopService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal; // Import Principal for authenticated user details
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional; // Import Optional

@RestController
@RequestMapping("/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    // --- Create Shop Endpoint ---
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createShop(
            Principal principal,
            @RequestPart("shop") @Valid ShopDTO shopDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null || principal.getName() == null) {
            logger.warn("Attempt to create shop without authenticated user.");
            response.put("success", false);
            response.put("message", "Authentication is required to create a shop.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Long ownerId = Long.parseLong(principal.getName());
            logger.info("Attempting to create shop '{}' for user ID: {}", shopDTO.getName(), ownerId);

            ShopDTO createdShop = shopService.createShop(shopDTO, logoFile, ownerId);
            logger.info("Shop created successfully with ID: {} by user ID: {}", createdShop.getId(), ownerId);

            response.put("success", true);
            response.put("message", "Shop created successfully!");
            response.put("shop", createdShop); // Add the created shopDTO to the response map
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format from principal: {}", principal.getName(), e);
            response.put("success", false);
            response.put("message", "Could not determine authenticated user ID. Invalid token format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating shop: {}", e.getMessage());
            response.put("success", false);
            response.put("message", "Error creating shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error during shop creation: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred during shop creation: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- Get All Shops Endpoint ---
    @GetMapping("getall")
    public ResponseEntity<Map<String, Object>> getAllShops() {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching all shops");
        try {
            List<ShopDTO> shops = shopService.getAllShops();
            if (shops.isEmpty()) {
                logger.warn("No shops found");
                response.put("success", true);
                response.put("message", "No shops found.");
                response.put("shops", shops);
                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            logger.info("Found {} shops", shops.size());
            response.put("success", true);
            response.put("message", "Shops retrieved successfully.");
            response.put("shops", shops);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching all shops: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to fetch shops: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- Get Shop by ID Endpoint ---
    @GetMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> getShopById(@PathVariable Long shopId) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching shop with ID: {}", shopId);
        try {
            return shopService.getShopById(shopId)
                    .map(shop -> {
                        ShopDTO shopDTO = shopService.convertToDTO(shop);
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
        } catch (Exception e) {
            logger.error("Error fetching shop with ID {}: {}", shopId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to fetch shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- Update Shop Endpoint ---
    @PutMapping(value = "/{shopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateShop(
            Principal principal,
            @PathVariable Long shopId,
            @RequestPart("shop") @Valid ShopDTO shopDTO,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null || principal.getName() == null) {
            logger.warn("Attempt to update shop {} without authenticated user.", shopId);
            response.put("success", false);
            response.put("message", "Authentication is required to update a shop.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        try {
            Long updaterId = Long.parseLong(principal.getName());
            logger.info("Attempting to update shop ID: {} by user ID: {}", shopId, updaterId);

            // Note: If you want to handle logoFile updates here, you'd extend shopService.updateShop
            // or call imageService directly for logo updates.
            ShopDTO updatedShop = shopService.updateShop(shopId, shopDTO, updaterId);
            logger.info("Shop ID: {} updated successfully by user ID: {}", updatedShop.getId(), updaterId);

            response.put("success", true);
            response.put("message", "Shop updated successfully!");
            response.put("shop", updatedShop);
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format from principal for update operation: {}", principal.getName(), e);
            response.put("success", false);
            response.put("message", "Could not determine authenticated user ID. Invalid token format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            logger.error("Error updating shop {}: {}", shopId, e.getMessage());
            response.put("success", false);
            response.put("message", "Failed to update shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error updating shop {}: {}", shopId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred during shop update: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- Delete Shop Endpoint ---
    @DeleteMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> deleteShop(
            Principal principal,
            @PathVariable Long shopId) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null || principal.getName() == null) {
            logger.warn("Attempt to delete shop {} without authenticated user.", shopId);
            response.put("success", false);
            response.put("message", "Authentication is required to delete a shop.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        logger.warn("Attempting to delete shop with ID: {}", shopId);
        try {
            Long deleterId = Long.parseLong(principal.getName());
            shopService.deleteShop(shopId, deleterId);
            logger.info("Shop with ID: {} deleted successfully by user ID: {}", shopId, deleterId);

            response.put("success", true);
            response.put("message", "Shop with ID: " + shopId + " deleted successfully.");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response); // 204 No Content
        } catch (NumberFormatException e) {
            logger.error("Invalid user ID format from principal for delete operation: {}", principal.getName(), e);
            response.put("success", false);
            response.put("message", "Could not determine authenticated user ID. Invalid token format.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (RuntimeException e) {
            logger.error("Failed to delete shop with ID: {}: {}", shopId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to delete shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (Exception e) {
            logger.error("Unexpected error deleting shop with ID: {}: {}", shopId, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred during shop deletion: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // --- Search Shops Endpoint ---
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchShops(@RequestParam String keyword) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Request received: Searching shops with keyword: {}", keyword);
        try {
            List<ShopDTO> shops = shopService.getShopsByName(keyword);
            response.put("success", true);
            response.put("message", "Shops searched successfully.");
            response.put("shops", shops);
            logger.info("Returning {} shops for keyword: {}", shops.size(), keyword);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error searching shops with keyword {}: {}", keyword, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to search shops: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}