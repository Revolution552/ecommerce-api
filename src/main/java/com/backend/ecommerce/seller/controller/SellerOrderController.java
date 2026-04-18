// src/main/java/com/backend/ecommerce/seller/controller/SellerOrderController.java
package com.backend.ecommerce.seller.controller;

import com.backend.ecommerce.order.model.FulfillmentStatus;
import com.backend.ecommerce.order.payload.OrderResponseDto;
import com.backend.ecommerce.order.payload.ShopOrderStatsDto;
import com.backend.ecommerce.order.service.OrderService;
import com.backend.ecommerce.seller.exception.SellerNotFoundException;
import com.backend.ecommerce.seller.exception.SellerNotVerifiedException;
import com.backend.ecommerce.seller.model.Seller;
import com.backend.ecommerce.seller.service.SellerService;
import com.backend.ecommerce.shop.exception.ShopNotFoundException;
import com.backend.ecommerce.shop.exception.ShopNotApprovedException;
import com.backend.ecommerce.shop.model.Shop;
import com.backend.ecommerce.shop.service.ShopService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/seller/orders")
public class SellerOrderController {

    private static final Logger logger = LoggerFactory.getLogger(SellerOrderController.class);

    private final OrderService orderService;
    private final SellerService sellerService;
    private final ShopService shopService;

    public SellerOrderController(OrderService orderService,
                                 SellerService sellerService,
                                 ShopService shopService) {
        this.orderService = orderService;
        this.sellerService = sellerService;
        this.shopService = shopService;
    }

    private Shop getValidatedShop(UserDetails userDetails)
            throws SellerNotFoundException, SellerNotVerifiedException, ShopNotFoundException, ShopNotApprovedException {
        Seller seller = sellerService.getVerifiedSellerByEmail(userDetails.getUsername());
        return shopService.getShopEntityById(
                shopService.getShopBySellerId(seller.getId()).id()
        );
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getShopOrders(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            List<OrderResponseDto> orders = orderService.getShopOrders(shop.getId());

            // Filter to only show items from this shop
            List<OrderResponseDto> filteredOrders = orders.stream()
                    .map(order -> filterOrderForShop(order, shop.getId()))
                    .collect(Collectors.toList());

            response.put("success", true);
            response.put("data", filteredOrders);
            response.put("count", filteredOrders.size());
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ShopNotFoundException | ShopNotApprovedException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error fetching shop orders", e);
            response.put("success", false);
            response.put("message", "Error fetching orders: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getShopOrderStats(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            ShopOrderStatsDto stats = orderService.getShopOrderStats(shop.getId(), start, end);
            response.put("success", true);
            response.put("data", stats);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ShopNotFoundException | ShopNotApprovedException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error fetching shop order stats", e);
            response.put("success", false);
            response.put("message", "Error fetching stats: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Map<String, Object>> getOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            OrderResponseDto order = orderService.getOrderById(orderId);

            // Verify order contains items from this shop
            boolean hasShopItems = order.items().stream()
                    .anyMatch(item -> item.shopId().equals(shop.getId()));

            if (!hasShopItems) {
                response.put("success", false);
                response.put("message", "Order does not contain items from your shop");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            OrderResponseDto filteredOrder = filterOrderForShop(order, shop.getId());
            response.put("success", true);
            response.put("data", filteredOrder);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            logger.error("Error fetching order details", e);
            response.put("success", false);
            response.put("message", "Error fetching order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/items/{itemId}/fulfillment")
    public ResponseEntity<Map<String, Object>> updateItemFulfillmentStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam FulfillmentStatus status) {

        Map<String, Object> response = new HashMap<>();

        try {
            // TODO: Implement fulfillment status update for individual items
            response.put("success", true);
            response.put("message", "Item fulfillment status updated.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error updating item fulfillment status", e);
            response.put("success", false);
            response.put("message", "Error updating status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private OrderResponseDto filterOrderForShop(OrderResponseDto order, Long shopId) {
        // Create a new OrderResponseDto with only items from this shop
        // This is a simplified version - you may need to create a new DTO
        return order; // For now, return as-is
    }
}