// src/main/java/com/backend/ecommerce/order/exception/OrderValidationException.java
package com.backend.ecommerce.order.exception;

public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}