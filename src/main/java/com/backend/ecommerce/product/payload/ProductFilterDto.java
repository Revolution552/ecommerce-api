// src/main/java/com/backend/ecommerce/product/payload/ProductFilterDto.java
package com.backend.ecommerce.product.payload;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductFilterDto {
    private String keyword;
    private Long categoryId;
    private List<Long> categoryIds;
    private Long shopId;
    private String brand;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean inStock;
    private Boolean onSale;
    private Boolean featured;
    private List<String> tags;
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
}