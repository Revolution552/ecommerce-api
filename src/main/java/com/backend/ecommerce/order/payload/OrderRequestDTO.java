package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.payments.PaymentRequestDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @NotNull(message = "Order details are required")
    @Valid
    private OrderDTO orderDetails;

    @NotNull(message = "Payment details are required")
    @Valid
    private PaymentRequestDTO paymentDetails;
}