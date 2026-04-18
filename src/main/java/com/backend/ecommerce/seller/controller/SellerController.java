// src/main/java/com/backend/ecommerce/seller/controller/SellerController.java
package com.backend.ecommerce.seller.controller;

import com.backend.ecommerce.seller.exception.SellerAlreadyExistsException;
import com.backend.ecommerce.seller.exception.SellerNotFoundException;
import com.backend.ecommerce.seller.payload.SellerRegistrationRequest;
import com.backend.ecommerce.seller.payload.SellerResponseDto;
import com.backend.ecommerce.seller.service.SellerService;
import com.backend.ecommerce.user.exception.EmailFailureException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerAsSeller(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SellerRegistrationRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            SellerResponseDto seller = sellerService.registerAsSeller(userDetails.getUsername(), request);
            response.put("success", true);
            response.put("message", "Seller registration submitted successfully. Please wait for admin verification.");
            response.put("data", seller);
            return ResponseEntity.ok(response);

        } catch (SellerAlreadyExistsException e) {
            logger.warn("Seller already exists for user: {}", userDetails.getUsername());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (EmailFailureException e) {
            logger.error("Email failure during seller registration", e);
            response.put("success", true);
            response.put("message", "Seller registration submitted but confirmation email could not be sent.");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error during seller registration", e);
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getSellerProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            SellerResponseDto seller = sellerService.getSellerProfile(userDetails.getUsername());
            response.put("success", true);
            response.put("data", seller);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSellerStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            SellerResponseDto seller = sellerService.getSellerProfile(userDetails.getUsername());
            response.put("success", true);
            response.put("isSeller", true);
            response.put("verificationStatus", seller.verificationStatus());
            response.put("isVerified", seller.verificationStatus().name().equals("APPROVED"));
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException e) {
            response.put("success", true);
            response.put("isSeller", false);
            response.put("message", "User is not registered as a seller");
            return ResponseEntity.ok(response);
        }
    }
}