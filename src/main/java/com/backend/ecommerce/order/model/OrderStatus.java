// src/main/java/com/backend/ecommerce/order/model/OrderStatus.java
package com.backend.ecommerce.order.model; // Changed package name

/**
 * Enum to define the possible statuses of an order.
 */
public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED
}
