// src/main/java/com/backend/ecommerce/seller/model/Seller.java
package com.backend.ecommerce.seller.model;

import com.backend.ecommerce.user.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "sellers")
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonIgnore
    private User user;

    @Column(name = "business_name", nullable = false)
    private String businessName;

    @Column(name = "business_email")
    private String businessEmail;

    @Column(name = "business_phone", nullable = false)
    private String businessPhone;

    @Column(name = "business_address", columnDefinition = "TEXT")
    private String businessAddress;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "id_card_number")
    private String idCardNumber;

    @Column(name = "bank_account_name")
    private String bankAccountName;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_name")
    private String bankName;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    private SellerVerificationStatus verificationStatus = SellerVerificationStatus.PENDING;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "verified_by")
    private Long verifiedBy; // Admin user ID

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}