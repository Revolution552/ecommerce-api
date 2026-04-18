// src/main/java/com/backend/ecommerce/admin/controller/AdminCategoryController.java
package com.backend.ecommerce.admin.controller;

import com.backend.ecommerce.category.exception.*;
import com.backend.ecommerce.category.payload.CategoryCreateRequest;
import com.backend.ecommerce.category.payload.CategoryResponseDto;
import com.backend.ecommerce.category.payload.CategoryTreeNodeDto;
import com.backend.ecommerce.category.payload.CategoryUpdateRequest;
import com.backend.ecommerce.category.service.CategoryService;
import com.backend.ecommerce.user.model.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(AdminCategoryController.class);

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createCategory(
            @Valid @RequestBody CategoryCreateRequest request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();

        try {
            CategoryResponseDto category = categoryService.createCategory(request, adminUser.getId());
            response.put("success", true);
            response.put("message", "Category created successfully.");
            response.put("data", category);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (CategoryAlreadyExistsException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception e) {
            logger.error("Error creating category", e);
            response.put("success", false);
            response.put("message", "Error creating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryResponseDto> categories = categoryService.getAllCategories();
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
    public ResponseEntity<Map<String, Object>> getFullCategoryTree() {
        Map<String, Object> response = new HashMap<>();

        try {
            List<CategoryTreeNodeDto> tree = categoryService.getCategoryTree();
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

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request,
            @AuthenticationPrincipal User adminUser) {

        Map<String, Object> response = new HashMap<>();

        try {
            CategoryResponseDto category = categoryService.updateCategory(id, request, adminUser.getId());
            response.put("success", true);
            response.put("message", "Category updated successfully.");
            response.put("data", category);
            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (CategoryAlreadyExistsException | CategoryCircularReferenceException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            logger.error("Error updating category", e);
            response.put("success", false);
            response.put("message", "Error updating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            categoryService.deleteCategory(id);
            response.put("success", true);
            response.put("message", "Category deleted successfully.");
            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (CategoryHasChildrenException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            logger.error("Error deleting category", e);
            response.put("success", false);
            response.put("message", "Error deleting category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<Map<String, Object>> toggleCategoryActive(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            CategoryResponseDto category = categoryService.getCategoryById(id);
            CategoryUpdateRequest request = new CategoryUpdateRequest();
            request.setIsActive(!category.isActive());

            CategoryResponseDto updatedCategory = categoryService.updateCategory(id, request, null);
            response.put("success", true);
            response.put("message", "Category status updated.");
            response.put("data", updatedCategory);
            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error toggling category active status", e);
            response.put("success", false);
            response.put("message", "Error updating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PatchMapping("/{id}/toggle-featured")
    public ResponseEntity<Map<String, Object>> toggleCategoryFeatured(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            CategoryResponseDto category = categoryService.getCategoryById(id);
            CategoryUpdateRequest request = new CategoryUpdateRequest();
            request.setIsFeatured(!category.isFeatured());

            CategoryResponseDto updatedCategory = categoryService.updateCategory(id, request, null);
            response.put("success", true);
            response.put("message", "Category featured status updated.");
            response.put("data", updatedCategory);
            return ResponseEntity.ok(response);

        } catch (CategoryNotFoundException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);

        } catch (Exception e) {
            logger.error("Error toggling category featured status", e);
            response.put("success", false);
            response.put("message", "Error updating category: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}