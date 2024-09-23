package com.backend.ecommerce.products.product.service;

import com.backend.ecommerce.products.category.service.CategoryService;
import com.backend.ecommerce.products.product.dao.ProductDAO;
import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.payload.ProductDTO;
import com.backend.ecommerce.users.model.LocalUser;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductDAO productDAO;
    private final CategoryService categoryService; // Add CategoryService for category validation
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    public ProductService(ProductDAO productDAO, CategoryService categoryService) {
        this.productDAO = productDAO;
        this.categoryService = categoryService;
    }

    public Product createProduct(ProductDTO productDTO, LocalUser user) {
        // Validate category ID
        if (productDTO.getCategoryId() == null || categoryService.getCategoryById(productDTO.getCategoryId()) == null) {
            logger.error("Invalid category ID: {}", productDTO.getCategoryId());
            throw new IllegalArgumentException("Invalid category ID");
        }

        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        product.setUser(user); // Associate product with the user
        product.setCategory(categoryService.getCategoryById(productDTO.getCategoryId())); // Set category

        Product savedProduct = productDAO.save(product);
        logger.info("Product created successfully: {}", savedProduct.getId());
        return savedProduct;
    }

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    @Transactional
    public Product updateProduct(Long productId, Product productDetails) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        logger.info("Product updated successfully: {}", product.getId());
        return productDAO.save(product);
    }

    public void deleteProduct(Long productId) {
        productDAO.deleteById(productId);
        logger.info("Product deleted successfully: {}", productId);
    }
}
