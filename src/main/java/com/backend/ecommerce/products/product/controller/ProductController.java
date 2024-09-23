package com.backend.ecommerce.products.product.controller;

import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.payload.ProductDTO;
import com.backend.ecommerce.products.product.service.ProductService;
import com.backend.ecommerce.users.model.LocalUser;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/product")
    public ResponseEntity<Product> createProduct(@AuthenticationPrincipal LocalUser user,
                                                 @Valid @RequestBody ProductDTO productDTO) {
        logger.info("User {} is attempting to create a new product: {}", user.getUsername(), productDTO.getName());
        Product product = productService.createProduct(productDTO, user);
        logger.info("Product {} created successfully by user {}", product.getName(), user.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productService.getAllProducts();
        logger.info("Returned {} products", products.size());
        return ResponseEntity.ok(products);
    }

    @PutMapping("/product/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @Valid @RequestBody Product productDetails) {
        logger.info("Attempting to update product with ID: {}", id);
        Product updatedProduct = productService.updateProduct(id, productDetails);
        logger.info("Product with ID: {} updated successfully", id);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        logger.warn("User attempting to delete product with ID: {}", id);
        productService.deleteProduct(id);
        logger.info("Product with ID: {} deleted successfully", id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getProductsByCategory(@PathVariable Long categoryId) {
        logger.info("Fetching products for category ID: {}", categoryId);

        List<Product> products = productService.getProductsByCategory(categoryId);

        if (products.isEmpty()) {
            logger.warn("No products found for category ID: {}", categoryId);
            return ResponseEntity.status(404).body("No products found for this category.");
        }

        logger.info("Found {} products for category ID: {}", products.size(), categoryId);
        return ResponseEntity.ok(products);
    }
}
