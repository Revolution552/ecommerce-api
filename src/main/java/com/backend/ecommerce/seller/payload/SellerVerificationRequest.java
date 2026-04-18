// src/main/java/com/backend/ecommerce/seller/payload/SellerVerificationRequest.java
package com.backend.ecommerce.seller.payload;

import com.backend.ecommerce.seller.model.SellerVerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SellerVerificationRequest {

    @NotNull(message = "Verification status is required")
    private SellerVerificationStatus status;

    private String notes;
}