package com.backend.ecommerce.users.api.model;

import jakarta.validation.constraints.*;

public class RegistrationBody {

    @NotBlank
    @NotNull
    @Size(min = 3, max = 20)
    private String username;

    @NotBlank
    @NotNull
    private String firstName;

    @NotBlank
    @NotNull
    private String lastName;

    @NotBlank
    @NotNull
    @Email
    private String email;

    @NotBlank
    @NotNull
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long")
    @Size(min=6, max=50)
    private String password;

    @NotBlank
    @NotNull
    private String confirmPassword;

    @NotNull(message = "Phone number cannot be null")
    @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be a valid 10-digit number starting with 0")
    private String phoneNumber;

    public void setUsername(@NotBlank @NotNull @Size(min = 3, max = 20) String username) {
        this.username = username;
    }

    public void setFirstName(@NotBlank @NotNull String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(@NotBlank @NotNull String lastName) {
        this.lastName = lastName;
    }

    public void setEmail(@NotBlank @NotNull @Email String email) {
        this.email = email;
    }

    public void setPassword(@NotBlank @NotNull @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long") @Size(min = 6, max = 50) String password) {
        this.password = password;
    }

    public void setConfirmPassword(@NotBlank @NotNull String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public void setPhoneNumber(@NotNull(message = "Phone number cannot be null") @Pattern(regexp = "^0\\d{9}$", message = "Phone number must be a valid 10-digit number starting with 0") String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
