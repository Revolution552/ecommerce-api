// src/main/java/com/backend/ecommerce/seller/controller/SellerShopController.java
package com.backend.ecommerce.seller.controller;

import com.backend.ecommerce.seller.exception.SellerNotFoundException;
import com.backend.ecommerce.seller.exception.SellerNotVerifiedException;
import com.backend.ecommerce.seller.service.SellerService;
import com.backend.ecommerce.shop.exception.ShopNotFoundException;
import com.backend.ecommerce.shop.payload.ShopResponseDto;
import com.backend.ecommerce.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/shop")
public class SellerShopController {

    private static final Logger logger = LoggerFactory.getLogger(SellerShopController.class);

    private final ShopService shopService;
    private final SellerService sellerService;

    public SellerShopController(ShopService shopService, SellerService sellerService) {
        this.shopService = shopService;
        this.sellerService = sellerService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyShop(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Get verified seller
            var seller = sellerService.getVerifiedSellerByEmail(userDetails.getUsername());

            // Get shop by seller ID
            ShopResponseDto shop = shopService.getShopBySellerId(seller.getId());

            response.put("success", true);
            response.put("data", shop);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ShopNotFoundException e) {
            response.put("success", false);
            response.put("message", "No shop found for your account. Please contact admin.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error fetching seller shop", e);
            response.put("success", false);
            response.put("message", "Error fetching shop: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMyShopStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            var seller = sellerService.getVerifiedSellerByEmail(userDetails.getUsername());
            ShopResponseDto shop = shopService.getShopBySellerId(seller.getId());

            response.put("success", true);
            response.put("hasShop", true);
            response.put("shopId", shop.id());
            response.put("shopName", shop.name());
            response.put("shopSlug", shop.slug());
            response.put("status", shop.status());
            response.put("isActive", shop.isActive());
            response.put("isApproved", shop.status().name().equals("APPROVED"));
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", true);
            response.put("hasShop", false);
            response.put("message", "You are not a verified seller");
            return ResponseEntity.ok(response);

        } catch (ShopNotFoundException e) {
            response.put("success", true);
            response.put("hasShop", false);
            response.put("message", "No shop assigned to your account");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching shop status", e);
            response.put("success", false);
            response.put("message", "Error fetching shop status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}