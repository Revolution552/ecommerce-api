// src/main/java/com/backend/ecommerce/admin/controller/AdminProductController.java
package com.backend.ecommerce.admin.controller;

import com.backend.ecommerce.product.exception.ProductNotFoundException;
import com.backend.ecommerce.product.model.ProductStatus;
import com.backend.ecommerce.product.payload.ProductResponseDto;
import com.backend.ecommerce.product.payload.ProductUpdateRequest;
import com.backend.ecommerce.product.service.ProductService;
import com.backend.ecommerce.user.model.User;
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
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private static final Logger logger = LoggerFactory.getLogger(AdminProductController.class);

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ProductResponseDto> products = productService.getAllProducts(pageable);
            response.put("success", true);
            response.put("data", products.getContent());
            response.put("totalElements", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("currentPage", products.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching products", e);
            response.put("success", false);
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockProducts() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductResponseDto> products = productService.getLowStockProducts();
            response.put("success", true);
            response.put("data", products);
            response.put("total", products.size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching low stock products", e);
            response.put("success", false);
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<Map<String, Object>> getProductsByShop(
            @PathVariable Long shopId,
            @PageableDefault(size = 20) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ProductResponseDto> products = productService.getProductsByShopPaginated(shopId, pageable);
            response.put("success", true);
            response.put("data", products.getContent());
            response.put("totalElements", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("currentPage", products.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching shop products", e);
            response.put("success", false);
            response.put("message", "Error fetching products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            ProductResponseDto product = productService.getProductById(productId);
            response.put("success", true);
            response.put("data", product);
            return ResponseEntity.ok(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable Long productId,
            @RequestBody ProductUpdateRequest request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();

        try {
            ProductResponseDto product = productService.updateProduct(productId, request, adminUser.getId());
            response.put("success", true);
            response.put("message", "Product updated successfully.");
            response.put("data", product);
            return ResponseEntity.ok(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error updating product", e);
            response.put("success", false);
            response.put("message", "Error updating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            productService.deleteProduct(productId);
            response.put("success", true);
            response.put("message", "Product deleted successfully.");
            return ResponseEntity.ok(response);

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

    @PatchMapping("/{productId}/feature")
    public ResponseEntity<Map<String, Object>> toggleFeatureProduct(@PathVariable Long productId) {
        Map<String, Object> response = new HashMap<>();

        try {
            ProductResponseDto existingProduct = productService.getProductById(productId);
            ProductUpdateRequest request = new ProductUpdateRequest();
            request.setIsFeatured(!existingProduct.isFeatured());

            ProductResponseDto product = productService.updateProduct(productId, request, null);
            response.put("success", true);
            response.put("message", "Product feature status updated.");
            response.put("data", product);
            return ResponseEntity.ok(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error toggling product feature", e);
            response.put("success", false);
            response.put("message", "Error updating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}