// src/main/java/com/backend/ecommerce/shop/payload/ShopApprovalRequest.java
package com.backend.ecommerce.shop.payload;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShopApprovalRequest {

    @NotNull(message = "Approval status is required")
    private Boolean approved;

    private String rejectionReason;
}