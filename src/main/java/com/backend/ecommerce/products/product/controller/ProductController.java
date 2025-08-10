package com.backend.ecommerce.products.product.controller;

import com.backend.ecommerce.products.product.payload.ProductRequestDTO;
import com.backend.ecommerce.products.product.payload.ProductResponseDTO;
import com.backend.ecommerce.products.product.service.ProductService;
import com.backend.ecommerce.user.model.User;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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

    /**
     * Creates a new product with an associated image.
     *
     * @param user The authenticated user creating the product.
     * @param productRequestDTO The product data transfer object for the request.
     * @param imageFile The image file for the product.
     * @return ResponseEntity with the created product or an error message.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createProduct(
            @AuthenticationPrincipal User user,
            @RequestPart("product") @Valid ProductRequestDTO productRequestDTO,
            @RequestPart("image") MultipartFile imageFile) {

        Map<String, Object> response = new HashMap<>();
        if (user == null) {
            logger.warn("Unauthorized attempt to create a product.");
            response.put("success", false);
            response.put("message", "Authentication is required to create a product.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        if (imageFile.isEmpty() || imageFile.getOriginalFilename() == null) {
            logger.warn("Image file is missing for new product creation.");
            response.put("success", false);
            response.put("message", "Product image is required.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        logger.info("User '{}' is creating a new product: '{}'", user.getUsername(), productRequestDTO.getName());
        try {
            ProductResponseDTO productResponseDTO = productService.createProduct(productRequestDTO, imageFile, user);
            logger.info("Product '{}' created successfully with ID: {}", productResponseDTO.getName(), productResponseDTO.getId());
            response.put("success", true);
            response.put("message", "Product created successfully!");
            response.put("product", productResponseDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            logger.error("Failed to create product for user '{}': {}", user.getUsername(), e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to create product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Retrieves all products with optional filtering, sorting, and ordering.
     *
     * @param minPrice Optional minimum price filter.
     * @param maxPrice Optional maximum price filter.
     * @param category Optional category name filter.
     * @param minRating Optional minimum rating filter.
     * @param sortBy The field to sort by (default: "price").
     * @param order The sort order (default: "asc").
     * @return A list of products matching the criteria.
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(defaultValue = "price") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching products with filters: minPrice={}, maxPrice={}, category={}, minRating={}, sortBy={}, order={}",
                minPrice, maxPrice, category, minRating, sortBy, order);

        List<ProductResponseDTO> products = productService.getAllProducts(minPrice, maxPrice, category, minRating, sortBy, order);

        if (products.isEmpty()) {
            logger.warn("No products found with the provided filters.");
            response.put("success", true);
            response.put("message", "No products found with the provided filters.");
            response.put("products", products);
            return ResponseEntity.ok(response);
        }

        logger.info("Found {} products matching the criteria.", products.size());
        response.put("success", true);
        response.put("message", "Products retrieved successfully.");
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves detailed information for a single product by its ID.
     *
     * @param id The ID of the product.
     * @return ResponseEntity with the product details or a 404 Not Found error.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProductDetails(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching details for product with ID: {}", id);

        return productService.getProductDetails(id)
                .map(productResponseDTO -> {
                    logger.info("Product details found for ID: {}", id);
                    response.put("success", true);
                    response.put("message", "Product details retrieved successfully.");
                    response.put("product", productResponseDTO);
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Product with ID: {} not found.", id);
                    response.put("success", false);
                    response.put("message", "Product with ID: " + id + " not found.");
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    /**
     * Updates an existing product.
     *
     * @param id The ID of the product to update.
     * @param productRequestDTO The updated product data.
     * @return ResponseEntity with the updated product or an error message.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(
            @PathVariable @Min(1) Long id,
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {

        Map<String, Object> response = new HashMap<>();
        logger.info("Attempting to update product with ID: {}", id);
        try {
            ProductResponseDTO updatedProductResponse = productService.updateProduct(id, productRequestDTO);
            logger.info("Product with ID: {} updated successfully.", id);
            response.put("success", true);
            response.put("message", "Product updated successfully.");
            response.put("product", updatedProductResponse);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating product with ID {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to update product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id The ID of the product to delete.
     * @return ResponseEntity indicating success or failure of the deletion.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable @Min(1) Long id) {
        Map<String, Object> response = new HashMap<>();
        logger.warn("User attempting to delete product with ID: {}", id);
        try {
            productService.deleteProduct(id);
            logger.info("Product with ID: {} deleted successfully.", id);
            response.put("success", true);
            response.put("message", "Product with ID: " + id + " deleted successfully.");
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            logger.error("Failed to delete product with ID {}: {}", id, e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to delete product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Retrieves all products belonging to a specific category.
     *
     * @param categoryId The ID of the category.
     * @return A list of products in the specified category.
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Map<String, Object>> getProductsByCategory(@PathVariable @Min(1) Long categoryId) {
        Map<String, Object> response = new HashMap<>();
        logger.info("Fetching products for category ID: {}", categoryId);

        List<ProductResponseDTO> products = productService.getProductsByCategory(categoryId);

        if (products.isEmpty()) {
            logger.warn("No products found for category ID: {}", categoryId);
            response.put("success", true);
            response.put("message", "No products found for category ID: " + categoryId + ".");
            response.put("products", products);
            return ResponseEntity.ok(response);
        }

        logger.info("Found {} products for category ID: {}", products.size(), categoryId);
        response.put("success", true);
        response.put("message", "Products for category " + categoryId + " retrieved successfully.");
        response.put("products", products);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches for products based on a keyword, category, and price range.
     *
     * @param keyword Optional keyword to search in product names and descriptions.
     * @param categoryId Optional category ID.
     * @param minPrice Optional minimum price.
     * @param maxPrice Optional maximum price.
     * @return A list of products matching the search criteria.
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        Map<String, Object> response = new HashMap<>();
        logger.info("Search request received: keyword='{}', categoryId={}, minPrice={}, maxPrice={}",
                keyword, categoryId, minPrice, maxPrice);

        List<ProductResponseDTO> products = productService.searchProducts(keyword, categoryId, minPrice, maxPrice);

        if (products.isEmpty()) {
            logger.warn("Search result: No products found.");
            response.put("success", true); // No products found is a valid outcome, not a failure.
            response.put("message", "No products found matching the search criteria.");
            response.put("products", products);
            return ResponseEntity.ok(response);
        }

        logger.info("Search result: {} products found.", products.size());
        response.put("success", true);
        response.put("message", "Products found successfully.");
        response.put("products", products);
        return ResponseEntity.ok(response);
    }
}