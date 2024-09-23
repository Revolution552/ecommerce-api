package com.backend.ecommerce.products.category.controller;

import com.backend.ecommerce.products.category.model.Category;
import com.backend.ecommerce.products.category.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        logger.info("Request received: Fetching all categories");
        List<Category> categories = categoryService.getAllCategories();
        logger.info("Returning {} categories", categories.size());
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        logger.info("Request received: Creating new category with name: {}", category.getName());
        Category createdCategory = categoryService.createCategory(category);
        logger.info("Category created successfully with ID: {}", createdCategory.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCategory);
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        logger.info("Request received: Fetching category with ID: {}", id);
        Category category = categoryService.getCategoryById(id);
        if (category == null) {
            logger.warn("Category with ID: {} not found", id);
            return ResponseEntity.notFound().build();
        }
        logger.info("Category with ID: {} found and returned", id);
        return ResponseEntity.ok(category);
    }
}
