// src/main/java/com/backend/ecommerce/order/model/Order.java
package com.backend.ecommerce.order.model;

import com.backend.ecommerce.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status")
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderShipping shipping;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderPayment payment;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "shipping_cost", precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "tax", precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "customer_notes", columnDefinition = "TEXT")
    private String customerNotes;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "coupon_code")
    private String couponCode;

    @Column(name = "currency", nullable = false)
    private String currency = "USD";

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "ordered_at")
    private LocalDateTime orderedAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (orderNumber == null) {
            orderNumber = generateOrderNumber();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" +
                String.format("%04d", (int)(Math.random() * 10000));
    }

    public void addStatusHistory(OrderStatus previousStatus, OrderStatus newStatus, String notes, User changedBy) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(this)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .notes(notes)
                .changedBy(changedBy)
                .build();
        statusHistory.add(history);
    }

    public void calculateTotals() {
        this.subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountAmount = subtotal.multiply(discount)
                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);

        this.total = subtotal
                .add(shippingCost)
                .add(tax)
                .subtract(discountAmount);

        if (this.total.compareTo(BigDecimal.ZERO) < 0) {
            this.total = BigDecimal.ZERO;
        }
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING ||
                status == OrderStatus.PROCESSING ||
                status == OrderStatus.CONFIRMED;
    }

    public boolean canBeRefunded() {
        return paymentStatus == PaymentStatus.PAID &&
                (status == OrderStatus.DELIVERED || status == OrderStatus.COMPLETED);
    }
}