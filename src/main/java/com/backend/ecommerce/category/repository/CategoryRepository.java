// src/main/java/com/backend/ecommerce/category/repository/CategoryRepository.java
package com.backend.ecommerce.category.repository;

import com.backend.ecommerce.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIsNull();

    List<Category> findByParentIsNullAndIsActiveTrue();

    List<Category> findByParentId(Long parentId);

    List<Category> findByParentIdAndIsActiveTrue(Long parentId);

    List<Category> findByIsActiveTrue();

    List<Category> findByIsFeaturedTrueAndIsActiveTrue();

    List<Category> findByShowInMenuTrueAndIsActiveTrue();

    List<Category> findByLevel(Integer level);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsByParentId(Long parentId);

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.sortOrder, c.name")
    List<Category> findAllRootCategoriesWithChildren();

    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.id = :id")
    Optional<Category> findByIdWithChildren(@Param("id") Long id);

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.level, c.sortOrder, c.name")
    List<Category> findAllActiveCategories();

    @Modifying
    @Query("UPDATE Category c SET c.productCount = c.productCount + 1 WHERE c.id = :categoryId")
    void incrementProductCount(@Param("categoryId") Long categoryId);

    @Modifying
    @Query("UPDATE Category c SET c.productCount = c.productCount - 1 WHERE c.id = :categoryId AND c.productCount > 0")
    void decrementProductCount(@Param("categoryId") Long categoryId);

    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "SELECT id, parent_id, name, slug, level FROM categories WHERE id = :categoryId " +
            "UNION ALL " +
            "SELECT c.id, c.parent_id, c.name, c.slug, c.level FROM categories c " +
            "INNER JOIN category_tree ct ON c.parent_id = ct.id" +
            ") SELECT * FROM category_tree", nativeQuery = true)
    List<Category> findCategoryWithAllDescendants(@Param("categoryId") Long categoryId);

    @Query(value = "WITH RECURSIVE category_tree AS (" +
            "SELECT id, parent_id, name, slug, level FROM categories WHERE id = :categoryId " +
            "UNION ALL " +
            "SELECT c.id, c.parent_id, c.name, c.slug, c.level FROM categories c " +
            "INNER JOIN category_tree ct ON c.id = ct.parent_id" +
            ") SELECT * FROM category_tree", nativeQuery = true)
    List<Category> findCategoryWithAllAncestors(@Param("categoryId") Long categoryId);
}