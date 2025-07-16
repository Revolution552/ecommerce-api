package com.backend.ecommerce.user.exception;

public class UserNotVerifiedException extends Exception {

    private final boolean newEmailSent;

    public UserNotVerifiedException(boolean newEmailSent) {
        super("User email is not verified. New verification email sent: " + newEmailSent);
        this.newEmailSent = newEmailSent;
    }

    public boolean isNewEmailSent() {
        return newEmailSent;
    }
}

