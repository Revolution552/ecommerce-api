package com.backend.ecommerce.users.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetResponse {
    private boolean success;
    private String message;
}
