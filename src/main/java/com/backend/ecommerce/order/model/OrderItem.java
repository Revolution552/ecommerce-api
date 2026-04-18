// src/main/java/com/backend/ecommerce/order/model/OrderItem.java
package com.backend.ecommerce.order.model;

import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.shop.model.Shop;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_sku")
    private String productSku;

    @Column(name = "product_image")
    private String productImage;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "compare_at_price", precision = 10, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "tax", precision = 10, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "fulfillment_status")
    private FulfillmentStatus fulfillmentStatus = FulfillmentStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateTotalPrice();
    }

    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public BigDecimal getSavings() {
        if (compareAtPrice != null && compareAtPrice.compareTo(unitPrice) > 0) {
            return compareAtPrice.subtract(unitPrice).multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }
}