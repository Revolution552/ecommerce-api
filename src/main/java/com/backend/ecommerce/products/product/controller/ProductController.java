package com.backend.ecommerce.products.product.controller;

import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.payload.ProductDTO;
import com.backend.ecommerce.products.product.service.ProductService;
import com.backend.ecommerce.user.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Product>> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false, defaultValue = "price") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String order) {

        logger.info("Fetching products with filters: minPrice={}, maxPrice={}, category={}, minRating={}, sortBy={}, order={}",
                minPrice, maxPrice, category, minRating, sortBy, order);

        List<Product> products = productService.getAllProducts(minPrice, maxPrice, category, minRating, sortBy, order);

        if (products.isEmpty()) {
            logger.warn("No products found with the provided filters.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(products);
        }

        logger.info("Found {} products matching the criteria", products.size());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ProductDTO> getProductDetails(@PathVariable Long id) {
        logger.info("Fetching details for product with ID: {}", id);

        return productService.getProductDetails(id)
                .map(productDTO -> {
                    logger.info("Product details found for ID: {}", id);
                    return ResponseEntity.ok(productDTO);
                })
                .orElseGet(() -> {
                    logger.warn("Product with ID: {} not found", id);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                });
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@AuthenticationPrincipal User user,
                                                 @Valid @RequestBody ProductDTO productDTO) {
        logger.info("User {} is attempting to create a new product: {}", user.getUsername(), productDTO.getName());

        Product product = productService.createProduct(productDTO, user);

        logger.info("Product '{}' created successfully by user {}", product.getName(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable @Min(1) Long id,
                                                 @Valid @RequestBody ProductDTO productDTO) {
        logger.info("Attempting to update product with ID: {}", id);

        Product updatedProduct = productService.updateProduct(id, productDTO);

        logger.info("Product with ID: {} updated successfully", id);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable @Min(1) Long id) {
        logger.warn("User attempting to delete product with ID: {}", id);

        productService.deleteProduct(id);

        logger.info("Product with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable @Min(1) Long categoryId) {
        logger.info("Fetching products for category ID: {}", categoryId);

        List<Product> products = productService.getProductsByCategory(categoryId);

        if (products.isEmpty()) {
            logger.warn("No products found for category ID: {}", categoryId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No products found for this category.");
        }

        logger.info("Found {} products for category ID: {}", products.size(), categoryId);
        return ResponseEntity.ok(products);
    }
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        logger.info("Search request received: keyword='{}', categoryId={}, minPrice={}, maxPrice={}",
                keyword, categoryId, minPrice, maxPrice);

        List<Product> products = productService.searchProducts(keyword, categoryId, minPrice, maxPrice);

        Map<String, Object> response = new HashMap<>();
        response.put("keyword", keyword);
        response.put("categoryId", categoryId);
        response.put("minPrice", minPrice);
        response.put("maxPrice", maxPrice);

        if (products.isEmpty()) {
            response.put("success", false);
            response.put("message", "No products found matching the search criteria.");
            logger.warn("Search result: No products found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        response.put("success", true);
        response.put("message", "Products found successfully.");
        response.put("products", products);
        logger.info("Search result: {} products found.", products.size());

        return ResponseEntity.ok(response);
    }

}
