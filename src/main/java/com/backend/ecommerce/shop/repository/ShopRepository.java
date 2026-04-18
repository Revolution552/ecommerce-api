// src/main/java/com/backend/ecommerce/shop/repository/ShopRepository.java
package com.backend.ecommerce.shop.repository;

import com.backend.ecommerce.seller.model.Seller;
import com.backend.ecommerce.shop.model.Shop;
import com.backend.ecommerce.shop.model.ShopStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Optional<Shop> findBySlug(String slug);

    Optional<Shop> findByName(String name);

    List<Shop> findBySeller(Seller seller);

    Optional<Shop> findBySellerId(Long sellerId);

    List<Shop> findByStatus(ShopStatus status);

    Page<Shop> findByStatus(ShopStatus status, Pageable pageable);

    List<Shop> findByIsActiveTrue();

    List<Shop> findByIsFeaturedTrueAndIsActiveTrue();

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    boolean existsBySellerId(Long sellerId);

    @Query("SELECT s FROM Shop s WHERE " +
            "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.city) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.state) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(s.country) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Shop> searchShops(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT s FROM Shop s WHERE s.status = 'APPROVED' AND s.isActive = true")
    List<Shop> findAllActiveApprovedShops();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.shop.id = :shopId")
    Integer countProductsByShopId(@Param("shopId") Long shopId);
}