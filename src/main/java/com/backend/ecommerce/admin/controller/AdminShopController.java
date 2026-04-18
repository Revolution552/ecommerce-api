// src/main/java/com/backend/ecommerce/admin/controller/AdminShopController.java
package com.backend.ecommerce.admin.controller;

import com.backend.ecommerce.shop.exception.InvalidSellerException;
import com.backend.ecommerce.shop.exception.ShopAlreadyExistsException;
import com.backend.ecommerce.shop.exception.ShopNotFoundException;
import com.backend.ecommerce.shop.model.ShopStatus;
import com.backend.ecommerce.shop.payload.ShopApprovalRequest;
import com.backend.ecommerce.shop.payload.ShopCreateRequest;
import com.backend.ecommerce.shop.payload.ShopResponseDto;
import com.backend.ecommerce.shop.payload.ShopUpdateRequest;
import com.backend.ecommerce.shop.service.ShopService;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/shops")
@PreAuthorize("hasRole('ADMIN')")
public class AdminShopController {

    private static final Logger logger = LoggerFactory.getLogger(AdminShopController.class);

    private final ShopService shopService;

    public AdminShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createShop(
            @Valid @RequestBody ShopCreateRequest request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();

        try {
            ShopResponseDto shop = shopService.createShop(request, adminUser.getId());
            response.put("success", true);
            response.put("message", "Shop created successfully. Email notification sent to seller.");
            response.put("data", shop);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (ShopAlreadyExistsException e) {
            logger.warn("Shop already exists: {}", request.getName());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (InvalidSellerException e) {
            logger.warn("Invalid seller for shop creation: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (EmailFailureException e) {
            logger.error("Email failure during shop creation", e);
            response.put("success", true);
            response.put("message", "Shop created but notification email could not be sent.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error creating shop", e);
            response.put("success", false);
            response.put("message", "Error creating shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{shopId}/approve")
    public ResponseEntity<Map<String, Object>> approveShop(
            @PathVariable Long shopId,
            @Valid @RequestBody ShopApprovalRequest request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();

        try {
            ShopResponseDto shop = shopService.approveShop(shopId, request, adminUser.getId());

            String message = request.getApproved()
                    ? "Shop has been approved successfully."
                    : "Shop has been rejected.";

            response.put("success", true);
            response.put("message", message);
            response.put("data", shop);
            return ResponseEntity.ok(response);

        } catch (ShopNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (EmailFailureException e) {
            logger.error("Failed to send approval email", e);
            response.put("success", true);
            response.put("message", "Shop status updated but notification email could not be sent.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error approving shop", e);
            response.put("success", false);
            response.put("message", "Error approving shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllShops(
            @RequestParam(required = false) ShopStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ShopResponseDto> shops;
            if (status != null) {
                shops = shopService.getShopsByStatusPaginated(status, pageable);
            } else {
                shops = shopService.getAllShopsPaginated(pageable);
            }

            response.put("success", true);
            response.put("data", shops.getContent());
            response.put("totalElements", shops.getTotalElements());
            response.put("totalPages", shops.getTotalPages());
            response.put("currentPage", shops.getNumber());
            response.put("pageSize", shops.getSize());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching shops", e);
            response.put("success", false);
            response.put("message", "Error fetching shops: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingShops() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ShopResponseDto> pendingShops = shopService.getShopsByStatus(ShopStatus.PENDING);
            response.put("success", true);
            response.put("data", pendingShops);
            response.put("total", pendingShops.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching pending shops", e);
            response.put("success", false);
            response.put("message", "Error fetching pending shops: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
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

    @PutMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> updateShop(
            @PathVariable Long shopId,
            @Valid @RequestBody ShopUpdateRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            ShopResponseDto shop = shopService.updateShop(shopId, request);
            response.put("success", true);
            response.put("message", "Shop updated successfully.");
            response.put("data", shop);
            return ResponseEntity.ok(response);

        } catch (ShopNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (ShopAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            logger.error("Error updating shop", e);
            response.put("success", false);
            response.put("message", "Error updating shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{shopId}")
    public ResponseEntity<Map<String, Object>> deleteShop(@PathVariable Long shopId) {
        Map<String, Object> response = new HashMap<>();

        try {
            shopService.deleteShop(shopId);
            response.put("success", true);
            response.put("message", "Shop deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (ShopNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error deleting shop", e);
            response.put("success", false);
            response.put("message", "Error deleting shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}