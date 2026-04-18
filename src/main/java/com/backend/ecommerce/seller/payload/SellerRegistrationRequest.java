// src/main/java/com/backend/ecommerce/seller/payload/SellerRegistrationRequest.java
package com.backend.ecommerce.seller.payload;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SellerRegistrationRequest {

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 100, message = "Business name must be between 2 and 100 characters")
    private String businessName;

    @Email(message = "Invalid business email format")
    private String businessEmail;

    @NotBlank(message = "Business phone is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String businessPhone;

    @NotBlank(message = "Business address is required")
    private String businessAddress;

    private String taxId;

    @NotBlank(message = "ID card number is required")
    private String idCardNumber;

    @NotBlank(message = "Bank account name is required")
    private String bankAccountName;

    @NotBlank(message = "Bank account number is required")
    private String bankAccountNumber;

    @NotBlank(message = "Bank name is required")
    private String bankName;
}