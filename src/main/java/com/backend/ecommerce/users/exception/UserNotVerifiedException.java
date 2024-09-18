package com.backend.ecommerce.users.exception;

public class UserNotVerifiedException extends Exception {

    private Boolean newEmailSent;

    public UserNotVerifiedException(Boolean newEmailSent) {
        this.newEmailSent = newEmailSent;
    }

    public Boolean istNewEmailSent() {
        return newEmailSent;
    }
}
