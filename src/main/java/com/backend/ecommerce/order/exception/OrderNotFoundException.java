// src/main/java/com/backend/ecommerce/order/exception/OrderNotFoundException.java
package com.backend.ecommerce.order.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}