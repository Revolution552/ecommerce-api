// src/main/java/com/backend/ecommerce/cart/model/Cart.java
package com.backend.ecommerce.cart.model;

import com.backend.ecommerce.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonIgnore
    private User user;

    @Column(name = "session_id", unique = true)
    private String sessionId;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<CartItem> items = new ArrayList<>();

    @Column(name = "total_items")
    private Integer totalItems = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateTotalItems() {
        this.totalItems = items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
    }
}