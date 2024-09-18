package com.backend.ecommerce.users.payload;

import com.backend.ecommerce.users.model.Address;

import java.util.List;

public class UserProfileDTO {
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private List<Address> addresses;

    public UserProfileDTO(String username, String firstName, String lastName, String email, List<Address> addresses) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.addresses = addresses;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }
}