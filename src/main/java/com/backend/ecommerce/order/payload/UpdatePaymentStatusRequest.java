// src/main/java/com/backend/ecommerce/order/payload/UpdatePaymentStatusRequest.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePaymentStatusRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    private String transactionId;
    private String paymentDetails;
    private String errorMessage;
}