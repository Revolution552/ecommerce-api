// src/main/java/com/backend/ecommerce/shop/payload/ShopUpdateRequest.java
package com.backend.ecommerce.shop.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShopUpdateRequest {

    @Size(min = 3, max = 100, message = "Shop name must be between 3 and 100 characters")
    private String name;

    @Size(max = 100, message = "Slug must not exceed 100 characters")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers, and hyphens")
    private String slug;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    private String description;

    private String logoUrl;
    private String bannerUrl;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    @Pattern(regexp = "^[0-9+\\-\\s()]+$", message = "Invalid phone number format")
    private String phone;

    @Email(message = "Invalid email format")
    private String email;

    private String website;
    private Boolean isActive;
    private Boolean isFeatured;
}