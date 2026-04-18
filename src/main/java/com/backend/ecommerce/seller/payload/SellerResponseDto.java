// src/main/java/com/backend/ecommerce/seller/payload/SellerResponseDto.java
package com.backend.ecommerce.seller.payload;

import com.backend.ecommerce.seller.model.SellerVerificationStatus;

import java.time.LocalDateTime;

public record SellerResponseDto(
        Long id,
        Long userId,
        String userEmail,
        String userFirstName,
        String userSurname,
        String businessName,
        String businessEmail,
        String businessPhone,
        String businessAddress,
        String taxId,
        String idCardNumber,
        String bankAccountName,
        String bankAccountNumber,
        String bankName,
        SellerVerificationStatus verificationStatus,
        String verificationNotes,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt,
        Boolean isActive
) {}