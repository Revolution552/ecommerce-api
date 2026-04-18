// src/main/java/com/backend/ecommerce/shop/model/ShopStatus.java
package com.backend.ecommerce.shop.model;

public enum ShopStatus {
    PENDING("Pending Approval"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    SUSPENDED("Suspended"),
    CLOSED("Closed");

    private final String description;

    ShopStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}