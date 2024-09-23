package com.backend.ecommerce.users.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordResponse {
    private boolean success;
    private String message;

    public ForgotPasswordResponse() {
    }

    public ForgotPasswordResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
