package com.backend.ecommerce.products.product.service;

import com.backend.ecommerce.products.category.model.Category;
import com.backend.ecommerce.products.category.service.CategoryService;
import com.backend.ecommerce.products.product.dao.ProductDAO;
import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.payload.ProductDTO;
import com.backend.ecommerce.products.shop.model.Shop;
import com.backend.ecommerce.products.shop.service.ShopService;
import com.backend.ecommerce.user.model.User;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductDAO productDAO;
    private final CategoryService categoryService;
    private final ShopService shopService;
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductDAO productDAO, CategoryService categoryService, ShopService shopService) {
        this.productDAO = productDAO;
        this.categoryService = categoryService;
        this.shopService = shopService;
    }

    public Product createProduct(ProductDTO productDTO, User user) {
        // Validate Category
        Category category = categoryService.getCategoryById(productDTO.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid category ID"));

        // Validate Shop
        Shop shop = shopService.getShopById(productDTO.getShopId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid shop ID"));

        // Validate Price
        if (productDTO.getPrice() <= 0) {
            logger.error("Product price must be greater than zero");
            throw new ValidationException("Product price must be greater than zero");
        }

        // Create new Product
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        product.setUser(user);
        product.setCategory(category);
        product.setShop(shop);

        // Save product to the database
        logger.info("Creating product: {}", productDTO.getName());
        return productDAO.save(product);
    }

    public List<Product> getAllProducts(Double minPrice, Double maxPrice, String category, Integer minRating, String sortBy, String order) {
        logger.info("Fetching products with filters: minPrice={}, maxPrice={}, category={}, minRating={}, sortBy={}, order={}",
                minPrice, maxPrice, category, minRating, sortBy, order);

        List<Product> products = productDAO.findWithFilters(minPrice, maxPrice, category, minRating, sortBy, order);
        if (products.isEmpty()) {
            logger.warn("No products found with the specified filters.");
        } else {
            logger.info("Found {} products with the filters", products.size());
        }
        return products;
    }

    public Optional<ProductDTO> getProductDetails(Long productId) {
        logger.info("Fetching details for product ID: {}", productId);

        return productDAO.findById(productId).map(product -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setDescription(product.getDescription());
            productDTO.setPrice(product.getPrice());
            productDTO.setImageUrl(product.getImageUrl());

            // Set category and shop information
            productDTO.setCategoryId(product.getCategory().getId());
            productDTO.setCategoryName(product.getCategory().getName());
            productDTO.setShopId(product.getShop().getId());
            productDTO.setShopName(product.getShop().getName());

            logger.info("Successfully retrieved product details: [ID: {}, Name: {}, Category: {}, Shop: {}]",
                    productDTO.getId(), productDTO.getName(), productDTO.getCategoryName(), productDTO.getShopName());

            return productDTO;

        });
    }

    @Transactional
    public Product updateProduct(Long productId, ProductDTO productDTO) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());

        // Validate category ID update if provided
        if (productDTO.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(productDTO.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category ID: " + productDTO.getCategoryId()));
            product.setCategory(category);
        }

        Product updatedProduct = productDAO.save(product);
        logger.info("Product updated successfully: ID={}", updatedProduct.getId());
        return updatedProduct;
    }

    public void deleteProduct(Long productId) {
        if (!productDAO.existsById(productId)) {
            logger.error("Attempted to delete non-existent product: ID={}", productId);
            throw new RuntimeException("Product not found");
        }
        productDAO.deleteById(productId);
        logger.info("Product deleted successfully: ID={}", productId);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        logger.info("Fetching products for category ID: {}", categoryId);

        List<Product> products = productDAO.findByCategoryId(categoryId);
        if (products.isEmpty()) {
            logger.warn("No products found for category ID: {}", categoryId);
        } else {
            logger.info("Found {} products for category ID: {}", products.size(), categoryId);
        }
        return products;
    }
    public List<Product> searchProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice) {
        List<Product> products = productDAO.searchProducts(keyword, categoryId, minPrice, maxPrice);

        if (products.isEmpty()) {
            logger.warn("No products found for the search criteria: keyword='{}', categoryId={}, minPrice={}, maxPrice={}",
                    keyword, categoryId, minPrice, maxPrice);
        } else {
            logger.info("Search successful. Found {} products for the criteria: keyword='{}', categoryId={}, minPrice={}, maxPrice={}",
                    products.size(), keyword, categoryId, minPrice, maxPrice);
        }

        return products;
    }
}
