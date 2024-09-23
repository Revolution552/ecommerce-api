package com.backend.ecommerce.users.api.model;

public class LoginResponse {
    private String jwt;
    private boolean success;
    private String message;
    private String failureReason;

    public LoginResponse(String jwt, boolean success, String message, String failureReason) {
        this.jwt = jwt;
        this.success = success;
        this.message = message;
        this.failureReason = failureReason;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
