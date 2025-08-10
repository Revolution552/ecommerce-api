// src/main/java/com/backend/ecommerce/payments/PaymentRequestDTO.java
package com.backend.ecommerce.payments;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO to handle incoming payment requests from the frontend.
 * This DTO centralizes payment information for different gateways.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // e.g., "PAYPAL", "THEWALLET"

    // --- TheWallet specific fields ---
    private String msisdn; // Customer's mobile number
    private String channel; // MNO channel, e.g., "TANZANIA.VODACOM"
    private String target; // USSD short code or merchant code

    // The payment amount will be taken from the order itself, not from the frontend DTO.
    // However, you might include a minimal amount for initial validation purposes
    // if your business logic requires it.
}