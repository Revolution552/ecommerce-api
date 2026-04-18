// src/main/java/com/backend/ecommerce/order/exception/InvalidOrderStatusTransitionException.java
package com.backend.ecommerce.order.exception;

public class InvalidOrderStatusTransitionException extends RuntimeException {
    public InvalidOrderStatusTransitionException(String message) {
        super(message);
    }
}