package com.backend.ecommerce.payments.model;

import com.backend.ecommerce.order.model.Order; // Assuming you have an Order entity in this path
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payments") // Table name for payments
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One-to-one relationship with the Order.
    // unique = true ensures that each order has only one payment record.
    // nullable = false means every payment MUST be linked to an order.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true, nullable = false)
    private Order order;

    @Column(nullable = false)
    private String provider; // e.g., "M-Pesa", "Tigo-Pesa", "Airtel-Money", "HALOTEL"

    @Column(nullable = false)
    private String providerTransactionId; // The unique transaction ID from the mobile money provider

    @Column(nullable = false)
    private String status; // Current status: PENDING, SUCCESS, FAILED, CANCELLED, etc.

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency; // e.g., "TZS"

    @Column(nullable = true) // Phone number involved in the transaction, can be null if not applicable or failed
    private String customerPhoneNumber;

    @Column(nullable = true) // Timestamp of the payment from the provider's callback (if available)
    private String paymentDate; // Storing as String for flexibility with external API date formats

    @Column(nullable = false)
    private LocalDateTime createdAt; // When this payment record was created in your system

    @Column(nullable = false)
    private LocalDateTime updatedAt; // Last time this payment record was updated

    // Optional: Store the raw callback payload for debugging/auditing purposes.
    // 'columnDefinition = "TEXT"' for databases like MySQL to allow larger text.
    @Column(columnDefinition = "TEXT")
    private String rawCallbackPayload;
}