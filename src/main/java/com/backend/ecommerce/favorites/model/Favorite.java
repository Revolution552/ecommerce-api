// src/main/java/com/backend/ecommerce/favorites/model/Favorite.java
package com.backend.ecommerce.favorites.model;

import com.backend.ecommerce.product.model.Product;
import com.backend.ecommerce.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "favorites", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "notes")
    private String notes;

    @Column(name = "notify_on_sale")
    private Boolean notifyOnSale = false;

    @Column(name = "notify_on_stock")
    private Boolean notifyOnStock = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}