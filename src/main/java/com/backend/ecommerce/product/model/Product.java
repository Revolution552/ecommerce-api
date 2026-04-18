// src/main/java/com/backend/ecommerce/product/model/Product.java
package com.backend.ecommerce.product.model;

import com.backend.ecommerce.category.model.Category;
import com.backend.ecommerce.shop.model.Shop;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;

    @Column(name = "sku_barcode")
    private String skuBarcode;

    @Column(name = "weight")
    private Double weight;

    @Column(name = "weight_unit")
    private String weightUnit = "kg";

    @Column(name = "dimensions")
    private String dimensions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonIgnore
    private Shop shop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private Category category;

    @Column(name = "brand")
    private String brand;

    @Column(name = "tags")
    private String tags;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_digital")
    private Boolean isDigital = false;

    @Column(name = "digital_file_url")
    private String digitalFileUrl;

    @Column(name = "low_stock_threshold")
    private Integer lowStockThreshold = 5;

    @Column(name = "main_image_url")
    private String mainImageUrl;

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Column(name = "meta_keywords")
    private String metaKeywords;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    private Integer totalReviews = 0;

    @Column(name = "total_sales")
    private Integer totalSales = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sku == null || sku.isEmpty()) {
            sku = generateSKU();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (status == ProductStatus.PUBLISHED && publishedAt == null) {
            publishedAt = LocalDateTime.now();
        }
    }

    private String generateSKU() {
        return "PRD-" + System.currentTimeMillis() + "-" +
                (shop != null ? shop.getId() : "XX") + "-" +
                (int)(Math.random() * 1000);
    }

    public Integer getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public boolean isInStock() {
        return getAvailableQuantity() > 0;
    }

    public boolean isLowStock() {
        return getAvailableQuantity() <= lowStockThreshold && getAvailableQuantity() > 0;
    }

    public boolean isOutOfStock() {
        return getAvailableQuantity() <= 0;
    }

    public BigDecimal getDiscountPercentage() {
        if (compareAtPrice != null && compareAtPrice.compareTo(BigDecimal.ZERO) > 0
                && price.compareTo(compareAtPrice) < 0) {
            return compareAtPrice.subtract(price)
                    .divide(compareAtPrice, 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        return BigDecimal.ZERO;
    }
}