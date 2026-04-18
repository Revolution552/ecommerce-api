// src/main/java/com/backend/ecommerce/category/payload/CategoryTreeNodeDto.java
package com.backend.ecommerce.category.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTreeNodeDto {
    private Long id;
    private String name;
    private String slug;
    private Long parentId;
    private Integer level;
    private Integer sortOrder;
    private Boolean isActive;
    private Long productCount;
    private List<CategoryTreeNodeDto> children;
}