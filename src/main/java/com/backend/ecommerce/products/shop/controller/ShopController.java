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

import java.util.List;

@RestController
@RequestMapping("/shops")
public class ShopController {

    private static final Logger logger = LoggerFactory.getLogger(ShopController.class);
    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("getall")
    public ResponseEntity<List<ShopDTO>> getAllShops() {
        logger.info("Fetching all shops");
        List<ShopDTO> shops = shopService.getAllShops();

        if (shops.isEmpty()) {
            logger.warn("No shops found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(shops);
        }

        logger.info("Found {} shops", shops.size());
        return ResponseEntity.ok(shops);
    }

    @PostMapping
    public ResponseEntity<ShopDTO> createShop(@Valid @RequestBody ShopDTO shopDTO) {
        logger.info("Creating a new shop with name: {}", shopDTO.getName());

        try {
            ShopDTO createdShop = shopService.createShop(shopDTO);
            logger.info("Shop created successfully with ID: {}", createdShop.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdShop);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating shop: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{shopId}")
    public ResponseEntity<ShopDTO> getShopById(@PathVariable Long shopId) {
        logger.info("Fetching shop with ID: {}", shopId);

        return shopService.getShopById(shopId)
                .map(shop -> {
                    logger.info("Shop found: {}", shop.getName());

                    // Convert Shop to ShopDTO
                    ShopDTO shopDTO = convertToDTO(shop);

                    return ResponseEntity.ok(shopDTO);
                })
                .orElseGet(() -> {
                    logger.warn("Shop with ID: {} not found", shopId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                });
    }

    // Mapper method to convert Shop to ShopDTO
    private ShopDTO convertToDTO(Shop shop) {
        ShopDTO dto = new ShopDTO();
        dto.setId(shop.getId());
        dto.setName(shop.getName());
        dto.setLocation(shop.getLocation());
        // Set other fields as necessary
        return dto;
    }


    @PutMapping("/{shopId}")
    public ResponseEntity<ShopDTO> updateShop(@PathVariable Long shopId, @Valid @RequestBody ShopDTO shopDTO) {
        logger.info("Updating shop with ID: {}", shopId);

        try {
            ShopDTO updatedShop = shopService.updateShop(shopId, shopDTO);
            logger.info("Shop updated successfully with ID: {}", updatedShop.getId());
            return ResponseEntity.ok(updatedShop);
        } catch (RuntimeException e) {
            logger.warn("Shop with ID: {} not found", shopId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long shopId) {
        logger.info("Attempting to delete shop with ID: {}", shopId);

        try {
            shopService.deleteShop(shopId);
            logger.info("Shop with ID: {} deleted successfully", shopId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            logger.error("Failed to delete shop with ID: {}", shopId, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
