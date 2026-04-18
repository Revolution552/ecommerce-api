// src/main/java/com/backend/ecommerce/order/payload/ShippingAddressRequest.java
package com.backend.ecommerce.order.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ShippingAddressRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String phone;

    @NotBlank(message = "Address line 1 is required")
    private String addressLine1;

    private String addressLine2;

    @NotBlank(message = "City is required")
    private String city;

    private String state;

    @NotBlank(message = "Postal code is required")
    private String postalCode;

    @NotBlank(message = "Country is required")
    private String country;
}