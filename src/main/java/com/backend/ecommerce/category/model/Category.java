// src/main/java/com/backend/ecommerce/category/model/Category.java
package com.backend.ecommerce.category.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url")
    private String iconUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "banner_url")
    private String bannerUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonIgnore
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, name ASC")
    private List<Category> children = new ArrayList<>();

    @Column(name = "level", nullable = false)
    private Integer level = 0;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "show_in_menu")
    private Boolean showInMenu = true;

    @Column(name = "meta_title")
    private String metaTitle;

    @Column(name = "meta_description")
    private String metaDescription;

    @Column(name = "meta_keywords")
    private String metaKeywords;

    @Column(name = "product_count")
    private Long productCount = 0L;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (parent != null) {
            level = parent.getLevel() + 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        if (parent != null) {
            level = parent.getLevel() + 1;
        }
    }

    public String getFullPath() {
        if (parent == null) {
            return "/" + slug;
        }
        return parent.getFullPath() + "/" + slug;
    }

    public List<Long> getAncestorIds() {
        List<Long> ancestors = new ArrayList<>();
        Category current = this.parent;
        while (current != null) {
            ancestors.add(0, current.getId());
            current = current.getParent();
        }
        return ancestors;
    }

    public boolean isAncestorOf(Category category) {
        if (category == null) return false;
        Category current = category.getParent();
        while (current != null) {
            if (current.getId().equals(this.id)) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }

    public boolean isDescendantOf(Category category) {
        return category != null && category.isAncestorOf(this);
    }
}