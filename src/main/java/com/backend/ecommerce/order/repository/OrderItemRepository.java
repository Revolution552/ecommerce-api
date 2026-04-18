// src/main/java/com/backend/ecommerce/order/repository/OrderItemRepository.java
package com.backend.ecommerce.order.repository;

import com.backend.ecommerce.order.model.OrderItem;
import com.backend.ecommerce.shop.model.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByShop(Shop shop);

    Page<OrderItem> findByShop(Shop shop, Pageable pageable);

    List<OrderItem> findByProductId(Long productId);

    @Query("SELECT oi FROM OrderItem oi WHERE oi.shop.id = :shopId")
    Page<OrderItem> findByShopId(@Param("shopId") Long shopId, Pageable pageable);

    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalSold FROM OrderItem oi " +
            "WHERE oi.order.paymentStatus = 'PAID' GROUP BY oi.product.id")
    List<Object[]> getProductSalesCounts();

    @Query("SELECT oi.product.id, SUM(oi.quantity) as totalSold FROM OrderItem oi " +
            "WHERE oi.shop.id = :shopId AND oi.order.paymentStatus = 'PAID' " +
            "GROUP BY oi.product.id")
    List<Object[]> getProductSalesCountsByShop(@Param("shopId") Long shopId);

    @Query("SELECT SUM(oi.totalPrice) FROM OrderItem oi WHERE oi.shop.id = :shopId " +
            "AND oi.order.paymentStatus = 'PAID'")
    BigDecimal calculateTotalRevenueByShop(@Param("shopId") Long shopId);

    @Query("SELECT SUM(oi.totalPrice) FROM OrderItem oi WHERE oi.shop.id = :shopId " +
            "AND oi.createdAt BETWEEN :start AND :end AND oi.order.paymentStatus = 'PAID'")
    BigDecimal calculateRevenueByShopForPeriod(@Param("shopId") Long shopId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end);
}