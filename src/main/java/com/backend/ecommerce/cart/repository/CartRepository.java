// src/main/java/com/backend/ecommerce/cart/repository/CartRepository.java
package com.backend.ecommerce.cart.repository;

import com.backend.ecommerce.cart.model.Cart;
import com.backend.ecommerce.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUserId(Long userId);

    Optional<Cart> findBySessionId(String sessionId);

    List<Cart> findByExpiresAtBefore(LocalDateTime now);

    boolean existsByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM Cart c WHERE c.expiresAt < :now")
    int deleteExpiredCarts(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Cart c SET c.expiresAt = :expiresAt WHERE c.id = :cartId")
    void updateExpiryDate(@Param("cartId") Long cartId, @Param("expiresAt") LocalDateTime expiresAt);
}