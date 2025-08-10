// src/main/java/com/backend/ecommerce/order/service/PaymentService.java
package com.backend.ecommerce.order.service;

import java.math.BigDecimal;

/**
 * Interface for a generic payment processing service.
 * This abstracts the actual payment gateway (e.g., PayPal, Stripe).
 */
public interface PaymentService {

    /**
     * Processes a payment for a given order.
     * @param orderId The ID of the order for which payment is being processed.
     * @param amount The total amount to be paid.
     * @return true if the payment was successful, false otherwise.
     * @throws Exception if an error occurs during payment processing.
     */
    boolean processPayment(Long orderId, BigDecimal amount) throws Exception;
}
