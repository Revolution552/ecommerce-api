package com.backend.ecommerce.products.product.service;

import com.backend.ecommerce.products.category.model.Category;
import com.backend.ecommerce.products.category.service.CategoryService;
import com.backend.ecommerce.products.product.dao.ProductDAO;
import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.payload.ProductRequestDTO;
import com.backend.ecommerce.products.product.payload.ProductResponseDTO;
import com.backend.ecommerce.products.shop.model.Shop;
import com.backend.ecommerce.products.shop.service.ShopService;
import com.backend.ecommerce.user.model.User;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductDAO productDAO;
    private final CategoryService categoryService;
    private final ShopService shopService;
    private final ImageService imageService;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductDAO productDAO, CategoryService categoryService, ShopService shopService, ImageService imageService) {
        this.productDAO = productDAO;
        this.categoryService = categoryService;
        this.shopService = shopService;
        this.imageService = imageService;
    }

    /**
     * Creates a new product and uploads the associated image to the S3 bucket.
     *
     * @param productRequestDTO The data transfer object containing product information.
     * @param imageFile The multipart file containing the product image.
     * @param user The user creating the product.
     * @return The newly created Product entity.
     */
    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO, MultipartFile imageFile, User user) {
        // Validate Category
        Category category = categoryService.getCategoryById(productRequestDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + productRequestDTO.getCategoryId()));

        // Validate Shop
        Shop shop = shopService.getShopById(productRequestDTO.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid shop ID: " + productRequestDTO.getShopId()));

        // Upload image and get the URL
        String imageUrl = imageService.uploadImage(imageFile);
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new RuntimeException("Failed to upload image.");
        }

        // Create new Product entity
        Product product = new Product();
        product.setName(productRequestDTO.getName());
        product.setDescription(productRequestDTO.getDescription());
        product.setPrice(productRequestDTO.getPrice());
        product.setImageUrl(imageUrl); // Set the URL from the uploaded image
        product.setUser(user);
        product.setCategory(category);
        product.setShop(shop);

        // Save product to the database
        Product savedProduct = productDAO.save(product);
        logger.info("Creating product '{}' for user '{}'", productRequestDTO.getName(), user.getUsername());

        return convertToResponseDTO(savedProduct);
    }

    /**
     * Retrieves all products with optional filtering, sorting, and ordering.
     *
     * @param minPrice Optional minimum price filter.
     * @param maxPrice Optional maximum price filter.
     * @param category Optional category name filter.
     * @param minRating Optional minimum rating filter.
     * @param sortBy The field to sort by.
     * @param order The sort order.
     * @return A list of products matching the criteria.
     */
    public List<ProductResponseDTO> getAllProducts(Double minPrice, Double maxPrice, String category, Integer minRating, String sortBy, String order) {
        logger.info("Fetching products with filters: minPrice={}, maxPrice={}, category={}, minRating={}, sortBy={}, order={}",
                minPrice, maxPrice, category, minRating, sortBy, order);

        List<Product> products = productDAO.findWithFilters(minPrice, maxPrice, category, minRating, sortBy, order);
        if (products.isEmpty()) {
            logger.warn("No products found with the specified filters.");
        } else {
            logger.info("Found {} products with the filters.", products.size());
        }

        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a DTO for a single product by its ID.
     *
     * @param productId The ID of the product.
     * @return An Optional containing the ProductResponseDTO or empty if not found.
     */
    public Optional<ProductResponseDTO> getProductDetails(Long productId) {
        logger.info("Fetching details for product ID: {}", productId);

        return productDAO.findById(productId).map(this::convertToResponseDTO);
    }

    /**
     * Updates an existing product.
     *
     * @param productId The ID of the product to update.
     * @param productRequestDTO The updated product data.
     * @return The updated Product entity.
     */
    @Transactional
    public ProductResponseDTO updateProduct(Long productId, ProductRequestDTO productRequestDTO) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        if (productRequestDTO.getName() != null) product.setName(productRequestDTO.getName());
        if (productRequestDTO.getDescription() != null) product.setDescription(productRequestDTO.getDescription());
        if (productRequestDTO.getPrice() != null && productRequestDTO.getPrice() > 0) product.setPrice(productRequestDTO.getPrice());

        // Update category if provided
        if (productRequestDTO.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(productRequestDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + productRequestDTO.getCategoryId()));
            product.setCategory(category);
        }

        Product updatedProduct = productDAO.save(product);
        logger.info("Product updated successfully: ID={}", updatedProduct.getId());

        return convertToResponseDTO(updatedProduct);
    }

    /**
     * Deletes a product by its ID and removes the associated image from S3.
     *
     * @param productId The ID of the product to delete.
     */
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + productId));

        // Delete image from S3
        String imageUrl = product.getImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            imageService.deleteImage(imageUrl);
        }

        productDAO.delete(product);
        logger.info("Product with ID: {} and its image deleted successfully.", productId);
    }

    /**
     * Retrieves all products belonging to a specific category.
     *
     * @param categoryId The ID of the category.
     * @return A list of products in the specified category.
     */
    public List<ProductResponseDTO> getProductsByCategory(Long categoryId) {
        logger.info("Fetching products for category ID: {}", categoryId);

        List<Product> products = productDAO.findByCategoryId(categoryId);
        if (products.isEmpty()) {
            logger.warn("No products found for category ID: {}", categoryId);
        } else {
            logger.info("Found {} products for category ID: {}", products.size(), categoryId);
        }

        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Searches for products based on a keyword, category name, and price range.
     * This method now specifically includes searching by product name using a keyword.
     *
     * @param keyword Optional keyword to search in product names and descriptions.
     * @param categoryName Optional category name.
     * @param minPrice Optional minimum price.
     * @param maxPrice Optional maximum price.
     * @return A list of products matching the search criteria.
     */
    public List<ProductResponseDTO> searchProducts(String keyword, String categoryName, Double minPrice, Double maxPrice) { // Changed categoryId to categoryName (String)
        logger.info("Searching products with criteria: keyword='{}', categoryName={}, minPrice={}, maxPrice={}", // Logging updated
                keyword, categoryName, minPrice, maxPrice);

        List<Product> products = productDAO.searchProducts(keyword, categoryName, minPrice, maxPrice); // Passed categoryName

        if (products.isEmpty()) {
            logger.warn("No products found for the search criteria.");
        } else {
            logger.info("Search successful. Found {} products for the criteria.", products.size());
        }

        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Searches for products specifically by their name (case-insensitive and partial match).
     *
     * @param name The name or part of the name to search for.
     * @return A list of products whose names contain the given keyword.
     */
    public List<ProductResponseDTO> getProductsByName(String name) {
        logger.info("Searching products by name: {}", name);
        // Assuming ProductDAO has a method like findByNameContainingIgnoreCase
        List<Product> products = productDAO.findByNameContainingIgnoreCase(name);
        if (products.isEmpty()) {
            logger.warn("No products found with name containing: {}", name);
        } else {
            logger.info("Found {} products with name containing: {}", products.size(), name);
        }
        return products.stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }


    /**
     * A helper method to convert a Product entity to a ProductResponseDTO.
     *
     * @param product The Product entity to convert.
     * @return The populated ProductResponseDTO.
     */
    private ProductResponseDTO convertToResponseDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId());
        dto.setCategoryName(product.getCategory().getName());
        dto.setShopId(product.getShop().getId());
        dto.setShopName(product.getShop().getName());
        return dto;
    }
}