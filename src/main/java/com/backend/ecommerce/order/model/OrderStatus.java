// src/main/java/com/backend/ecommerce/order/model/OrderStatus.java
package com.backend.ecommerce.order.model;

public enum OrderStatus {
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    PROCESSING("Processing"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded"),
    ON_HOLD("On Hold"),
    PAYMENT_FAILED("Payment Failed");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}