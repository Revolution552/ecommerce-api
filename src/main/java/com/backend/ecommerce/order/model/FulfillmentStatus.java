// src/main/java/com/backend/ecommerce/order/model/FulfillmentStatus.java
package com.backend.ecommerce.order.model;

public enum FulfillmentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    PICKED("Picked"),
    PACKED("Packed"),
    SHIPPED("Shipped"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String description;

    FulfillmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}