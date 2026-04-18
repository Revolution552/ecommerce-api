// src/main/java/com/backend/ecommerce/category/payload/CategoryUpdateRequest.java
package com.backend.ecommerce.category.payload;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryUpdateRequest {

    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String name;

    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String iconUrl;
    private String imageUrl;
    private String bannerUrl;

    private Long parentId;

    @Min(value = 0, message = "Sort order cannot be negative")
    private Integer sortOrder;

    private Boolean isActive;
    private Boolean isFeatured;
    private Boolean showInMenu;

    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
}