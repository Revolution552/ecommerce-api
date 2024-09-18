package com.backend.ecommerce.users.api.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordResetBody {

    @NotBlank
    @NotNull
    private String token;

    @NotBlank
    @NotNull
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long")
    @Size(min=6, max=50)
    private String newPassword;

    @NotBlank
    @NotNull
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long")
    @Size(min=6, max=50)
    private String confirmPassword;

    public @NotBlank @NotNull String getToken() {
        return token;
    }

    public void setToken(@NotBlank @NotNull String token) {
        this.token = token;
    }

    public @NotBlank @NotNull @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long") @Size(min = 6, max = 50)
    String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(@NotBlank @NotNull @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long") @Size(min = 6, max = 50)
                               String newPassword) {
        this.newPassword = newPassword;
    }

    public @NotBlank @NotNull @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long") @Size(min = 6, max = 50)
    String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(@NotBlank @NotNull @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
            message = "Password must contain at least one letter, one number, and be at least 8 characters long") @Size(min = 6, max = 50)
                                   String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public boolean isPasswordConfirmed() {
        return this.newPassword != null && this.newPassword.equals(this.confirmPassword);
    }
}
