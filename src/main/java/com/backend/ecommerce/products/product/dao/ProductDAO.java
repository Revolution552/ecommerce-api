package com.backend.ecommerce.products.product.dao;

import com.backend.ecommerce.products.product.model.Product;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ProductDAO extends ListCrudRepository<Product, Long> {

    @Query("SELECT p FROM Product p " +
            "WHERE (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
            "AND (:category IS NULL OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :category, '%'))) " +
            "AND (:minRating IS NULL OR p.rating >= :minRating) " +
            "ORDER BY " +
            "CASE WHEN :order = 'asc' THEN " +
            "     CASE WHEN :sortBy = 'price' THEN p.price END " +
            "END ASC, " +
            "CASE WHEN :order = 'desc' THEN " +
            "     CASE WHEN :sortBy = 'price' THEN p.price END " +
            "END DESC")
    List<Product> findWithFilters(
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("category") String category,
            @Param("minRating") Integer minRating,
            @Param("sortBy") String sortBy,
            @Param("order") String order
    );

    // Updated to use categoryName instead of categoryId
    @Query("SELECT p FROM Product p " +
            "WHERE (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryName IS NULL OR LOWER(p.category.name) LIKE LOWER(CONCAT('%', :categoryName, '%'))) " + // Changed here
            "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
            "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryName") String categoryName, // Changed parameter type and name
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByNameContainingIgnoreCase(String name);

}