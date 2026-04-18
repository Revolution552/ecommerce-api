// src/main/java/com/backend/ecommerce/order/payload/CreateOrderRequest.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull(message = "Items are required")
    @Valid
    private List<OrderItemRequest> items;

    @NotNull(message = "Shipping address is required")
    @Valid
    private ShippingAddressRequest shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private String couponCode;
    private String customerNotes;
    private String shippingMethod;
    private boolean useWalletBalance = false;
}