// src/main/java/com/backend/ecommerce/order/model/PaymentStatus.java
package com.backend.ecommerce.order.model;

public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    PAID("Paid"),
    FAILED("Failed"),
    REFUNDED("Refunded"),
    PARTIALLY_REFUNDED("Partially Refunded");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}