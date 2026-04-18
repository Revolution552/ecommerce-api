// src/main/java/com/backend/ecommerce/order/model/PaymentMethod.java
package com.backend.ecommerce.order.model;

public enum PaymentMethod {
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    PAYPAL("PayPal"),
    BANK_TRANSFER("Bank Transfer"),
    CASH_ON_DELIVERY("Cash on Delivery"),
    WALLET("Wallet"),
    STRIPE("Stripe");

    private final String description;

    PaymentMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}