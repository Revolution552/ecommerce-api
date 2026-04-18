// src/main/java/com/backend/ecommerce/product/repository/ProductRepository.java
package com.backend.ecommerce.product.repository;

import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.product.model.ProductStatus;
import com.backend.ecommerce.shop.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    Optional<Product> findBySku(String sku);

    List<Product> findByShop(Shop shop);

    Page<Product> findByShop(Shop shop, Pageable pageable);

    List<Product> findByShopId(Long shopId);

    Page<Product> findByShopId(Long shopId, Pageable pageable);

    List<Product> findByCategoryId(Long categoryId);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    List<Product> findByStatus(ProductStatus status);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);

    List<Product> findByShopIdAndStatus(Long shopId, ProductStatus status);

    List<Product> findByIsFeaturedTrueAndStatus(ProductStatus status);

    List<Product> findByBrand(String brand);

    boolean existsByNameAndShopId(String name, Long shopId);

    boolean existsBySlug(String slug);

    boolean existsBySku(String sku);

    @Query("SELECT p FROM Product p WHERE " +
            "p.status = 'PUBLISHED' AND " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.quantity <= p.lowStockThreshold AND p.quantity > 0")
    List<Product> findLowStockProducts();

    @Query("SELECT p FROM Product p WHERE p.quantity = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.compareAtPrice > p.price AND p.status = 'PUBLISHED'")
    List<Product> findOnSaleProducts();

    @Modifying
    @Query("UPDATE Product p SET p.viewCount = p.viewCount + 1 WHERE p.id = :productId")
    void incrementViewCount(@Param("productId") Long productId);

    @Modifying
    @Query("UPDATE Product p SET p.quantity = p.quantity - :quantity, " +
            "p.reservedQuantity = p.reservedQuantity + :quantity " +
            "WHERE p.id = :productId AND p.quantity >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.reservedQuantity = p.reservedQuantity - :quantity, " +
            "p.quantity = p.quantity - :quantity, " +
            "p.totalSales = p.totalSales + :quantity " +
            "WHERE p.id = :productId AND p.reservedQuantity >= :quantity")
    int confirmStockReduction(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Modifying
    @Query("UPDATE Product p SET p.reservedQuantity = p.reservedQuantity - :quantity, " +
            "p.quantity = p.quantity + :quantity " +
            "WHERE p.id = :productId AND p.reservedQuantity >= :quantity")
    int releaseReservedStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    @Query("SELECT p FROM Product p WHERE p.shop.id = :shopId AND p.quantity <= p.lowStockThreshold")
    List<Product> findLowStockProductsByShop(@Param("shopId") Long shopId);

    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId AND p.status = 'PUBLISHED'")
    Long countPublishedProductsByShop(@Param("shopId") Long shopId);
}