// src/main/java/com/backend/ecommerce/seller/controller/SellerProductController.java
package com.backend.ecommerce.seller.controller;

import com.backend.ecommerce.product.exception.InsufficientStockException;
import com.backend.ecommerce.product.exception.ProductAlreadyExistsException;
import com.backend.ecommerce.product.exception.ProductNotFoundException;
import com.backend.ecommerce.product.payload.ProductCreateRequest;
import com.backend.ecommerce.product.payload.ProductResponseDto;
import com.backend.ecommerce.product.payload.ProductUpdateRequest;
import com.backend.ecommerce.product.service.ProductService;
import com.backend.ecommerce.seller.exception.SellerNotFoundException;
import com.backend.ecommerce.seller.exception.SellerNotVerifiedException;
import com.backend.ecommerce.seller.model.Seller;
import com.backend.ecommerce.seller.service.SellerService;
import com.backend.ecommerce.shop.exception.ShopNotApprovedException;
import com.backend.ecommerce.shop.model.Shop;
import com.backend.ecommerce.shop.service.ShopService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/seller/products")
public class SellerProductController {

    private static final Logger logger = LoggerFactory.getLogger(SellerProductController.class);

    private final ProductService productService;
    private final SellerService sellerService;
    private final ShopService shopService;

    public SellerProductController(ProductService productService,
                                   SellerService sellerService,
                                   ShopService shopService) {
        this.productService = productService;
        this.sellerService = sellerService;
        this.shopService = shopService;
    }

    private Shop getValidatedShop(UserDetails userDetails)
            throws SellerNotFoundException, SellerNotVerifiedException, ShopNotApprovedException {
        Seller seller = sellerService.getVerifiedSellerByEmail(userDetails.getUsername());
        return shopService.getShopEntityById(
                shopService.getShopBySellerId(seller.getId()).id()
        );
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProductCreateRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            request.setShopId(shop.getId());

            ProductResponseDto product = productService.createProduct(request, shop.getSeller().getUser().getId());
            response.put("success", true);
            response.put("message", "Product created successfully.");
            response.put("data", product);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ShopNotApprovedException e) {
            response.put("success", false);
            response.put("message", "Your shop is not approved: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ProductAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            logger.error("Error creating product", e);
            response.put("success", false);
            response.put("message", "Error creating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            Page<ProductResponseDto> products = productService.getProductsByShopPaginated(shop.getId(), pageable);

            response.put("success", true);
            response.put("data", products.getContent());
            response.put("totalElements", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("currentPage", products.getNumber());
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ShopNotApprovedException e) {
            response.put("success", false);
            response.put("message", "Your shop is not approved: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            logger.error("Error fetching products", e);
            response.put("success", false);
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockProducts(
            @AuthenticationPrincipal UserDetails userDetails) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            List<ProductResponseDto> products = productService.getLowStockProductsByShop(shop.getId());

            response.put("success", true);
            response.put("data", products);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ShopNotApprovedException e) {
            response.put("success", false);
            response.put("message", "Your shop is not approved: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (Exception e) {
            logger.error("Error fetching low stock products", e);
            response.put("success", false);
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            ProductResponseDto product = productService.getProductById(productId);

            // Verify product belongs to seller's shop
            if (!product.shopId().equals(shop.getId())) {
                response.put("success", false);
                response.put("message", "You don't have permission to view this product");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            response.put("success", true);
            response.put("data", product);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error fetching product", e);
            response.put("success", false);
            response.put("message", "Error fetching product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequest request) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            ProductResponseDto existingProduct = productService.getProductById(productId);

            // Verify product belongs to seller's shop
            if (!existingProduct.shopId().equals(shop.getId())) {
                response.put("success", false);
                response.put("message", "You don't have permission to update this product");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            ProductResponseDto product = productService.updateProduct(productId, request, shop.getSeller().getUser().getId());
            response.put("success", true);
            response.put("message", "Product updated successfully.");
            response.put("data", product);
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (ProductAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            logger.error("Error updating product", e);
            response.put("success", false);
            response.put("message", "Error updating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            ProductResponseDto existingProduct = productService.getProductById(productId);

            // Verify product belongs to seller's shop
            if (!existingProduct.shopId().equals(shop.getId())) {
                response.put("success", false);
                response.put("message", "You don't have permission to delete this product");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            productService.deleteProduct(productId);
            response.put("success", true);
            response.put("message", "Product deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (SellerNotFoundException | SellerNotVerifiedException e) {
            response.put("success", false);
            response.put("message", "You are not a verified seller: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error deleting product", e);
            response.put("success", false);
            response.put("message", "Error deleting product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/{productId}/stock/reserve")
    public ResponseEntity<Map<String, Object>> reserveStock(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        Map<String, Object> response = new HashMap<>();

        try {
            Shop shop = getValidatedShop(userDetails);
            ProductResponseDto existingProduct = productService.getProductById(productId);

            if (!existingProduct.shopId().equals(shop.getId())) {
                response.put("success", false);
                response.put("message", "You don't have permission to manage this product");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }

            boolean reserved = productService.reserveStock(productId, quantity);
            response.put("success", true);
            response.put("reserved", reserved);
            response.put("message", reserved ? "Stock reserved successfully" : "Failed to reserve stock");
            return ResponseEntity.ok(response);

        } catch (InsufficientStockException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error reserving stock", e);
            response.put("success", false);
            response.put("message", "Error reserving stock: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}