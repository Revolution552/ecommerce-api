package com.backend.ecommerce.user.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordResponse {
    private boolean success;
    private String message;

}
