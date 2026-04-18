// src/main/java/com/backend/ecommerce/favorites/repository/FavoriteRepository.java
package com.backend.ecommerce.favorites.repository;

import com.backend.ecommerce.favorites.model.Favorite;
import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndProduct(User user, Product product);

    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);

    List<Favorite> findByUser(User user);

    Page<Favorite> findByUser(User user, Pageable pageable);

    Page<Favorite> findByUserId(Long userId, Pageable pageable);

    List<Favorite> findByUserId(Long userId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);

    void deleteByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.product.id IN :productIds")
    void deleteByUserIdAndProductIds(@Param("userId") Long userId, @Param("productIds") List<Long> productIds);

    @Query("SELECT COUNT(f) FROM Favorite f WHERE f.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT f.product.id FROM Favorite f WHERE f.user.id = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.product p JOIN FETCH p.shop WHERE f.user.id = :userId")
    List<Favorite> findByUserIdWithProductDetails(@Param("userId") Long userId);

    @Query("SELECT f FROM Favorite f JOIN FETCH f.product WHERE f.user.id = :userId " +
            "AND (LOWER(f.product.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(f.product.shop.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Favorite> searchUserFavorites(@Param("userId") Long userId,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    @Query("SELECT f FROM Favorite f WHERE f.notifyOnSale = true AND f.product.compareAtPrice > f.product.price")
    List<Favorite> findFavoritesWithSaleNotifications();

    @Query("SELECT f FROM Favorite f WHERE f.notifyOnStock = true AND f.product.quantity > 0 " +
            "AND f.product.quantity <= f.product.lowStockThreshold")
    List<Favorite> findFavoritesWithStockNotifications();

    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.product.id = :productId")
    void deleteByProductId(@Param("productId") Long productId);
}