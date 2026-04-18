// src/main/java/com/backend/ecommerce/cart/repository/CartItemRepository.java
package com.backend.ecommerce.cart.repository;

import com.backend.ecommerce.cart.model.Cart;
import com.backend.ecommerce.cart.model.CartItem;
import com.backend.ecommerce.product.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    List<CartItem> findByCart(Cart cart);

    List<CartItem> findByCartId(Long cartId);

    List<CartItem> findByCartIdAndIsSelectedTrue(Long cartId);

    List<CartItem> findByCartIdAndShopId(Long cartId, Long shopId);

    void deleteByCartIdAndProductId(Long cartId, Long productId);

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id IN :productIds")
    void deleteByCartIdAndProductIds(@Param("cartId") Long cartId, @Param("productIds") List<Long> productIds);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isSelected = :isSelected WHERE ci.cart.id = :cartId")
    void updateSelectionForCart(@Param("cartId") Long cartId, @Param("isSelected") Boolean isSelected);

    @Modifying
    @Query("UPDATE CartItem ci SET ci.isSelected = :isSelected WHERE ci.cart.id = :cartId AND ci.shop.id = :shopId")
    void updateSelectionForShop(@Param("cartId") Long cartId, @Param("shopId") Long shopId,
                                @Param("isSelected") Boolean isSelected);

    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId")
    Integer countItemsByCartId(@Param("cartId") Long cartId);

    @Query("SELECT COALESCE(SUM(ci.totalPrice), 0) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.isSelected = true")
    java.math.BigDecimal calculateSelectedSubtotal(@Param("cartId") Long cartId);
}