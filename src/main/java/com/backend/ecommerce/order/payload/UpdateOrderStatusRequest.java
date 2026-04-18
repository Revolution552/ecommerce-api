// src/main/java/com/backend/ecommerce/order/payload/UpdateOrderStatusRequest.java
package com.backend.ecommerce.order.payload;

import com.backend.ecommerce.order.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrderStatusRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    private String notes;
    private String trackingNumber;
    private String trackingUrl;
    private String carrier;
}