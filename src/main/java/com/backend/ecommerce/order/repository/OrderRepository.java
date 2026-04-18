// src/main/java/com/backend/ecommerce/order/repository/OrderRepository.java
package com.backend.ecommerce.order.repository;

import com.backend.ecommerce.order.model.Order;
import com.backend.ecommerce.order.model.OrderStatus;
import com.backend.ecommerce.order.model.PaymentStatus;
import com.backend.ecommerce.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByUser(User user);

    Page<Order> findByUser(User user, Pageable pageable);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    List<Order> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE " +
            "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.user.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(o.user.surname) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Order> searchOrders(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.user.id = :userId AND o.paymentStatus = 'PAID'")
    BigDecimal calculateTotalSpentByUser(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.paymentStatus = 'PENDING' AND o.createdAt < :cutoffTime")
    List<Order> findPendingPaymentsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);

    @Query("SELECT COUNT(o), SUM(o.total) FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    Object[] getOrderStatsForPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query(value = "SELECT DATE(o.created_at) as date, COUNT(*) as count, SUM(o.total) as total " +
            "FROM orders o WHERE o.created_at >= :startDate GROUP BY DATE(o.created_at) " +
            "ORDER BY date DESC", nativeQuery = true)
    List<Object[]> getDailyOrderStats(@Param("startDate") LocalDateTime startDate);
}