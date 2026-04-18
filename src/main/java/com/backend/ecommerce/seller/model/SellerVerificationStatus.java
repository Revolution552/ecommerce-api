// src/main/java/com/backend/ecommerce/seller/model/SellerVerificationStatus.java
package com.backend.ecommerce.seller.model;

public enum SellerVerificationStatus {
    PENDING("Pending Verification"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended");

    private final String description;

    SellerVerificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}