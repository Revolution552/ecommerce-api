package com.backend.ecommerce.products.category.controller;

import com.backend.ecommerce.products.category.model.Category;
import com.backend.ecommerce.products.category.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api") // Keep this base path for all category endpoints
public class CategoryController {

    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        logger.info("Request received: Fetching all categories");
        List<Category> categories = categoryService.getAllCategories();
        logger.info("Returning {} categories", categories.size());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Categories fetched successfully.");
        response.put("categories", categories);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        logger.info("Request received: Fetching category with ID: {}", id);
        Optional<Category> category = categoryService.getCategoryById(id);

        Map<String, Object> response = new HashMap<>();
        if (category.isPresent()) {
            response.put("success", true);
            response.put("message", "Category fetched successfully.");
            response.put("category", category.get());
            logger.info("Category with ID: {} found", id);
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Category not found.");
            logger.warn("Category with ID: {} not found", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * Endpoint to search for categories by name, supporting partial and case-insensitive matching.
     *
     * @param keyword The search term to find within category names.
     * @return A ResponseEntity containing a list of matching categories.
     */
    @GetMapping("/categories/search")
    public ResponseEntity<Map<String, Object>> searchCategories(@RequestParam String keyword) {
        logger.info("Request received: Searching categories with keyword: {}", keyword);
        List<Category> categories = categoryService.searchCategoriesByName(keyword);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Categories searched successfully.");
        response.put("categories", categories);
        logger.info("Returning {} categories for keyword: {}", categories.size(), keyword);
        return ResponseEntity.ok(response);
    }
}
