// src/main/java/com/backend/ecommerce/product/payload/ProductCreateRequest.java
package com.backend.ecommerce.product.payload;

import com.backend.ecommerce.product.model.ProductStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateRequest {

    @NotBlank(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    private String name;

    @Size(max = 200, message = "Slug must not exceed 200 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;

    private String sku;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    private String shortDescription;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @DecimalMax(value = "9999999.99", message = "Price must not exceed 9,999,999.99")
    private BigDecimal price;

    @DecimalMin(value = "0.0", inclusive = true, message = "Compare at price must be >= 0")
    private BigDecimal compareAtPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Cost price must be >= 0")
    private BigDecimal costPrice;

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    private Integer quantity = 0;

    private String skuBarcode;
    private Double weight;
    private String weightUnit;
    private String dimensions;

    @NotNull(message = "Shop ID is required")
    private Long shopId;

    private Long categoryId;
    private String brand;
    private List<String> tags;

    private ProductStatus status = ProductStatus.DRAFT;
    private Boolean isFeatured = false;
    private Boolean isDigital = false;
    private String digitalFileUrl;
    private Integer lowStockThreshold = 5;

    private String mainImageUrl;
    private List<String> images;
    private String videoUrl;

    private String metaTitle;
    private String metaDescription;
    private String metaKeywords;
}