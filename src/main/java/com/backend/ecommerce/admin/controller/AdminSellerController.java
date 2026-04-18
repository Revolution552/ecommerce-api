// src/main/java/com/backend/ecommerce/admin/controller/AdminSellerController.java
package com.backend.ecommerce.admin.controller;

import com.backend.ecommerce.seller.exception.SellerNotFoundException;
import com.backend.ecommerce.seller.model.SellerVerificationStatus;
import com.backend.ecommerce.seller.payload.SellerResponseDto;
import com.backend.ecommerce.seller.payload.SellerVerificationRequest;
import com.backend.ecommerce.seller.service.SellerService;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private static final Logger logger = LoggerFactory.getLogger(AdminSellerController.class);

    private final SellerService sellerService;

    public AdminSellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSellers(
            @RequestParam(required = false) SellerVerificationStatus status) {

        Map<String, Object> response = new HashMap<>();

        try {
            List<SellerResponseDto> sellers;
            if (status != null) {
                sellers = sellerService.getSellersByStatus(status);
            } else {
                sellers = sellerService.getAllSellers();
            }

            response.put("success", true);
            response.put("data", sellers);
            response.put("total", sellers.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching sellers", e);
            response.put("success", false);
            response.put("message", "Error fetching sellers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{sellerId}")
    public ResponseEntity<Map<String, Object>> getSellerById(@PathVariable Long sellerId) {
        Map<String, Object> response = new HashMap<>();

        try {
            SellerResponseDto seller = sellerService.getSellerById(sellerId);
            response.put("success", true);
            response.put("data", seller);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/pending")
    public ResponseEntity<Map<String, Object>> getPendingSellers() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<SellerResponseDto> pendingSellers = sellerService.getSellersByStatus(SellerVerificationStatus.PENDING);
            response.put("success", true);
            response.put("data", pendingSellers);
            response.put("total", pendingSellers.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching pending sellers", e);
            response.put("success", false);
            response.put("message", "Error fetching pending sellers: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{sellerId}/verify")
    public ResponseEntity<Map<String, Object>> verifySeller(
            @PathVariable Long sellerId,
            @Valid @RequestBody SellerVerificationRequest request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();

        try {
            SellerResponseDto updatedSeller = sellerService.verifySeller(sellerId, request, adminUser.getId());

            String message;
            if (request.getStatus() == SellerVerificationStatus.APPROVED) {
                message = "Seller has been approved successfully.";
            } else if (request.getStatus() == SellerVerificationStatus.REJECTED) {
                message = "Seller has been rejected.";
            } else {
                message = "Seller status updated to: " + request.getStatus();
            }

            response.put("success", true);
            response.put("message", message);
            response.put("data", updatedSeller);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (EmailFailureException e) {
            logger.error("Failed to send verification email", e);
            response.put("success", true);
            response.put("message", "Seller status updated but notification email could not be sent.");
            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            logger.error("Error verifying seller", e);
            response.put("success", false);
            response.put("message", "Error verifying seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}