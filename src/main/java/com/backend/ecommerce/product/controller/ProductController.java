// src/main/java/com/backend/ecommerce/product/controller/ProductController.java
package com.backend.ecommerce.product.controller;

import com.backend.ecommerce.product.exception.ProductNotFoundException;
import com.backend.ecommerce.product.payload.ProductFilterDto;
import com.backend.ecommerce.product.payload.ProductResponseDto;
import com.backend.ecommerce.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getPublishedProducts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ProductResponseDto> products = productService.getPublishedProducts(pageable);
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

    @PostMapping("/filter")
    public ResponseEntity<Map<String, Object>> filterProducts(
            @RequestBody ProductFilterDto filter,
            @PageableDefault(size = 20) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ProductResponseDto> products = productService.filterProducts(filter, pageable);
            response.put("success", true);
            response.put("data", products.getContent());
            response.put("totalElements", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("currentPage", products.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error filtering products", e);
            response.put("success", false);
            response.put("message", "Error filtering products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {

        Map<String, Object> response = new HashMap<>();

        try {
            Page<ProductResponseDto> products = productService.searchProducts(keyword, pageable);
            response.put("success", true);
            response.put("data", products.getContent());
            response.put("totalElements", products.getTotalElements());
            response.put("totalPages", products.getTotalPages());
            response.put("currentPage", products.getNumber());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error searching products", e);
            response.put("success", false);
            response.put("message", "Error searching products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedProducts() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductResponseDto> products = productService.getFeaturedProducts();
            response.put("success", true);
            response.put("data", products);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching featured products", e);
            response.put("success", false);
            response.put("message", "Error fetching featured products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/on-sale")
    public ResponseEntity<Map<String, Object>> getOnSaleProducts() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductResponseDto> products = productService.getOnSaleProducts();
            response.put("success", true);
            response.put("data", products);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching on-sale products", e);
            response.put("success", false);
            response.put("message", "Error fetching on-sale products: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable Long categoryId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<ProductResponseDto> products = productService.getProductsByCategory(categoryId);
            response.put("success", true);
            response.put("data", products);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching products by category", e);
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

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> getProductBySlug(@PathVariable String slug) {
        Map<String, Object> response = new HashMap<>();

        try {
            ProductResponseDto product = productService.getProductBySlug(slug);
            response.put("success", true);
            response.put("data", product);
            return ResponseEntity.ok(response);

        } catch (ProductNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
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
}