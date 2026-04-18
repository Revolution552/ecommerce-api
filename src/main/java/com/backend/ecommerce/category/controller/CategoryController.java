// src/main/java/com/backend/ecommerce/category/controller/CategoryController.java
package com.backend.ecommerce.category.controller;

import com.backend.ecommerce.category.exception.CategoryNotFoundException;
import com.backend.ecommerce.category.payload.CategoryResponseDto;
import com.backend.ecommerce.category.payload.CategoryTreeNodeDto;
import com.backend.ecommerce.category.service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getActiveCategories() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryResponseDto> categories = categoryService.getActiveCategories();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching categories", e);
            response.put("success", false);
            response.put("message", "Error fetching categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/tree")
    public ResponseEntity<Map<String, Object>> getCategoryTree() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryTreeNodeDto> tree = categoryService.getActiveCategoryTree();
            response.put("success", true);
            response.put("data", tree);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching category tree", e);
            response.put("success", false);
            response.put("message", "Error fetching category tree: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/featured")
    public ResponseEntity<Map<String, Object>> getFeaturedCategories() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryResponseDto> categories = categoryService.getFeaturedCategories();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching featured categories", e);
            response.put("success", false);
            response.put("message", "Error fetching featured categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/menu")
    public ResponseEntity<Map<String, Object>> getMenuCategories() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryResponseDto> categories = categoryService.getMenuCategories();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching menu categories", e);
            response.put("success", false);
            response.put("message", "Error fetching menu categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/root")
    public ResponseEntity<Map<String, Object>> getRootCategories() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryResponseDto> categories = categoryService.getActiveRootCategories();
            response.put("success", true);
            response.put("data", categories);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching root categories", e);
            response.put("success", false);
            response.put("message", "Error fetching root categories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<Map<String, Object>> getSubcategories(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryResponseDto> subcategories = categoryService.getActiveSubcategories(id);
            response.put("success", true);
            response.put("data", subcategories);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching subcategories", e);
            response.put("success", false);
            response.put("message", "Error fetching subcategories: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/{id}/breadcrumb")
    public ResponseEntity<Map<String, Object>> getCategoryBreadcrumb(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryResponseDto> breadcrumb = categoryService.getCategoryBreadcrumb(id);
            response.put("success", true);
            response.put("data", breadcrumb);
            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error fetching category breadcrumb", e);
            response.put("success", false);
            response.put("message", "Error fetching breadcrumb: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<Map<String, Object>> getCategoryBySlug(@PathVariable String slug) {
        Map<String, Object> response = new HashMap<>();

        try {
            CategoryResponseDto category = categoryService.getCategoryBySlug(slug);
            response.put("success", true);
            response.put("data", category);
            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCategoryById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            CategoryResponseDto category = categoryService.getCategoryById(id);
            response.put("success", true);
            response.put("data", category);
            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}