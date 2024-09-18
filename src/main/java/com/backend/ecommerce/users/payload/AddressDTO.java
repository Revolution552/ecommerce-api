package com.backend.ecommerce.users.payload;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {


    @NotEmpty(message = "Street must not be empty")
    @Size(max = 100, message = "Street cannot exceed 100 characters")
    private String street;

    @NotEmpty(message = "City must not be empty")
    @Size(max = 50, message = "City cannot exceed 50 characters")
    private String city;

    @NotEmpty(message = "Country must not be empty")
    @Size(max = 75, message = "Country cannot exceed 75 characters")
    private String country;

    @NotEmpty(message = "Postal code must not be empty")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    private String postalCode;

    public AddressDTO() {}

    public AddressDTO(String street, String city, String country, String postalCode) {
        this.street = street;
        this.city = city;
        this.country = country;
        this.postalCode = postalCode;
    }
}

