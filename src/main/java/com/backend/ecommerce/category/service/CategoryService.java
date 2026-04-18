// src/main/java/com/backend/ecommerce/category/service/CategoryService.java
package com.backend.ecommerce.category.service;

import com.backend.ecommerce.category.exception.*;
import com.backend.ecommerce.category.model.Category;
import com.backend.ecommerce.category.payload.CategoryCreateRequest;
import com.backend.ecommerce.category.payload.CategoryResponseDto;
import com.backend.ecommerce.category.payload.CategoryTreeNodeDto;
import com.backend.ecommerce.category.payload.CategoryUpdateRequest;
import com.backend.ecommerce.category.repository.CategoryRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public CategoryResponseDto createCategory(CategoryCreateRequest request, Long userId)
            throws CategoryAlreadyExistsException, CategoryNotFoundException {

        logger.info("Creating category: {}", request.getName());

        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryAlreadyExistsException("Category with name '" + request.getName() + "' already exists");
        }

        // Generate slug if not provided
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        }

        // Check if slug already exists
        if (categoryRepository.existsBySlug(slug)) {
            throw new CategoryAlreadyExistsException("Category with slug '" + slug + "' already exists");
        }

        // Validate parent if provided
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found with ID: " + request.getParentId()));
        }

        // Create category
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .iconUrl(request.getIconUrl())
                .imageUrl(request.getImageUrl())
                .bannerUrl(request.getBannerUrl())
                .parent(parent)
                .sortOrder(request.getSortOrder())
                .isActive(request.getIsActive())
                .isFeatured(request.getIsFeatured())
                .showInMenu(request.getShowInMenu())
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .productCount(0L)
                .createdBy(userId)
                .build();

        Category savedCategory = categoryRepository.save(category);
        logger.info("Category created successfully: {} with slug: {}", savedCategory.getName(), savedCategory.getSlug());
        return mapToResponseDto(savedCategory);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long categoryId, CategoryUpdateRequest request, Long userId)
            throws CategoryNotFoundException, CategoryAlreadyExistsException, CategoryCircularReferenceException {

        logger.info("Updating category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        // Update fields
        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(request.getName())) {
                throw new CategoryAlreadyExistsException("Category with name '" + request.getName() + "' already exists");
            }
            category.setName(request.getName());
        }

        if (request.getSlug() != null && !request.getSlug().equals(category.getSlug())) {
            if (categoryRepository.existsBySlug(request.getSlug())) {
                throw new CategoryAlreadyExistsException("Category with slug '" + request.getSlug() + "' already exists");
            }
            category.setSlug(request.getSlug());
        }

        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getIconUrl() != null) category.setIconUrl(request.getIconUrl());
        if (request.getImageUrl() != null) category.setImageUrl(request.getImageUrl());
        if (request.getBannerUrl() != null) category.setBannerUrl(request.getBannerUrl());
        if (request.getSortOrder() != null) category.setSortOrder(request.getSortOrder());
        if (request.getIsActive() != null) category.setIsActive(request.getIsActive());
        if (request.getIsFeatured() != null) category.setIsFeatured(request.getIsFeatured());
        if (request.getShowInMenu() != null) category.setShowInMenu(request.getShowInMenu());
        if (request.getMetaTitle() != null) category.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null) category.setMetaDescription(request.getMetaDescription());
        if (request.getMetaKeywords() != null) category.setMetaKeywords(request.getMetaKeywords());

        // Handle parent update
        if (request.getParentId() != null) {
            if (request.getParentId().equals(categoryId)) {
                throw new CategoryCircularReferenceException("Category cannot be its own parent");
            }

            Category newParent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryNotFoundException("Parent category not found with ID: " + request.getParentId()));

            // Check for circular reference
            if (newParent.isDescendantOf(category)) {
                throw new CategoryCircularReferenceException("Cannot set a descendant as parent");
            }

            category.setParent(newParent);
        } else if (request.getParentId() == null && category.getParent() != null) {
            category.setParent(null);
        }

        category.setUpdatedBy(userId);

        Category updatedCategory = categoryRepository.save(category);
        logger.info("Category updated successfully: {}", updatedCategory.getName());
        return mapToResponseDto(updatedCategory);
    }

    @Transactional
    public void deleteCategory(Long categoryId)
            throws CategoryNotFoundException, CategoryHasChildrenException {

        logger.info("Deleting category with ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        // Check if category has children
        if (!category.getChildren().isEmpty()) {
            throw new CategoryHasChildrenException("Cannot delete category with subcategories. Delete subcategories first.");
        }

        // Check if category has products (you may want to add this check)
        if (category.getProductCount() > 0) {
            throw new CategoryHasChildrenException("Cannot delete category with associated products. Reassign products first.");
        }

        categoryRepository.delete(category);
        logger.info("Category deleted successfully with ID: {}", categoryId);
    }

    public CategoryResponseDto getCategoryById(Long categoryId) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));
        return mapToResponseDto(category);
    }

    public CategoryResponseDto getCategoryBySlug(String slug) throws CategoryNotFoundException {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with slug: " + slug));
        return mapToResponseDto(category);
    }

    public List<CategoryResponseDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getRootCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getActiveRootCategories() {
        return categoryRepository.findByParentIsNullAndIsActiveTrue().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getActiveSubcategories(Long parentId) {
        return categoryRepository.findByParentIdAndIsActiveTrue(parentId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getFeaturedCategories() {
        return categoryRepository.findByIsFeaturedTrueAndIsActiveTrue().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getMenuCategories() {
        return categoryRepository.findByShowInMenuTrueAndIsActiveTrue().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryTreeNodeDto> getCategoryTree() {
        List<Category> rootCategories = categoryRepository.findByParentIsNull();
        return rootCategories.stream()
                .map(this::buildTreeNode)
                .collect(Collectors.toList());
    }

    public List<CategoryTreeNodeDto> getActiveCategoryTree() {
        List<Category> activeRootCategories = categoryRepository.findByParentIsNullAndIsActiveTrue();
        return activeRootCategories.stream()
                .map(this::buildActiveTreeNode)
                .filter(node -> node != null)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getCategoryWithAncestors(Long categoryId) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        List<Category> ancestors = new ArrayList<>();
        Category current = category.getParent();
        while (current != null) {
            ancestors.add(0, current);
            current = current.getParent();
        }

        return ancestors.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<CategoryResponseDto> getCategoryBreadcrumb(Long categoryId) throws CategoryNotFoundException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));

        List<Category> breadcrumb = new ArrayList<>();
        Category current = category;
        while (current != null) {
            breadcrumb.add(0, current);
            current = current.getParent();
        }

        return breadcrumb.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void incrementProductCount(Long categoryId) {
        categoryRepository.incrementProductCount(categoryId);
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null && category.getParent() != null) {
            incrementProductCount(category.getParent().getId());
        }
    }

    @Transactional
    public void decrementProductCount(Long categoryId) {
        categoryRepository.decrementProductCount(categoryId);
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category != null && category.getParent() != null) {
            decrementProductCount(category.getParent().getId());
        }
    }

    public Category getCategoryEntityById(Long categoryId) throws CategoryNotFoundException {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with ID: " + categoryId));
    }

    private CategoryTreeNodeDto buildTreeNode(Category category) {
        List<CategoryTreeNodeDto> children = category.getChildren().stream()
                .map(this::buildTreeNode)
                .collect(Collectors.toList());

        return CategoryTreeNodeDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .level(category.getLevel())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .productCount(category.getProductCount())
                .children(children.isEmpty() ? null : children)
                .build();
    }

    private CategoryTreeNodeDto buildActiveTreeNode(Category category) {
        if (!category.getIsActive()) {
            return null;
        }

        List<CategoryTreeNodeDto> children = category.getChildren().stream()
                .map(this::buildActiveTreeNode)
                .filter(node -> node != null)
                .collect(Collectors.toList());

        return CategoryTreeNodeDto.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .level(category.getLevel())
                .sortOrder(category.getSortOrder())
                .isActive(category.getIsActive())
                .productCount(category.getProductCount())
                .children(children.isEmpty() ? null : children)
                .build();
    }

    private String generateSlug(String input) {
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NON_LATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        return slug.toLowerCase(Locale.ENGLISH);
    }

    private CategoryResponseDto mapToResponseDto(Category category) {
        List<CategoryResponseDto> children = category.getChildren() != null ?
                category.getChildren().stream()
                        .map(this::mapToResponseDto)
                        .collect(Collectors.toList()) :
                List.of();

        return new CategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getIconUrl(),
                category.getImageUrl(),
                category.getBannerUrl(),
                category.getParent() != null ? category.getParent().getId() : null,
                category.getParent() != null ? category.getParent().getName() : null,
                category.getLevel(),
                category.getSortOrder(),
                category.getIsActive(),
                category.getIsFeatured(),
                category.getShowInMenu(),
                category.getFullPath(),
                category.getMetaTitle(),
                category.getMetaDescription(),
                category.getMetaKeywords(),
                category.getProductCount(),
                children,
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }
}