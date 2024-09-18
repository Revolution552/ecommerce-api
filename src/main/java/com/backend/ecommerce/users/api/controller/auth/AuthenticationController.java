package com.backend.ecommerce.users.api.controller.auth;

import com.backend.ecommerce.users.api.model.LoginBody;
import com.backend.ecommerce.users.api.model.PasswordResetBody;
import com.backend.ecommerce.users.api.model.RegistrationBody;
import com.backend.ecommerce.users.exception.EmailFailureException;
import com.backend.ecommerce.users.exception.EmailNotFoundException;
import com.backend.ecommerce.users.exception.UserAlreadyExistsException;
import com.backend.ecommerce.users.exception.UserNotVerifiedException;
import com.backend.ecommerce.users.model.LocalUser;
import com.backend.ecommerce.users.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    private final UserService userService;

    public AuthenticationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody RegistrationBody registrationBody) {
        Map<String, String> response = new HashMap<>();

        try {
            userService.registerUser(registrationBody);
            logger.info("User registered successfully: {}", registrationBody.getEmail());

            response.put("message", "User registered successfully!");
            response.put("status", "OK");
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (UserAlreadyExistsException ex) {
            logger.warn("User registration failed - user already exists: {}", registrationBody.getEmail());

            response.put("message", "User already exists!");
            response.put("status", "CONFLICT");
            return new ResponseEntity<>(response, HttpStatus.CONFLICT);

        } catch (EmailFailureException ex) {
            logger.error("Failed to send confirmation email for user: {}", registrationBody.getEmail(), ex);

            response.put("message", "Failed to send confirmation email!");
            response.put("status", "INTERNAL_SERVER_ERROR");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);

        } catch (Exception ex) {
            logger.error("An unexpected error occurred during registration: {}", ex.getMessage(), ex);

            response.put("message", "An unexpected error occurred: " + ex.getMessage());
            response.put("status", "INTERNAL_SERVER_ERROR");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@Valid @RequestBody LoginBody loginBody) {
        Map<String, Object> response = new HashMap<>();
        String jwt;

        try {
            jwt = userService.loginUser(loginBody);
            if (jwt != null) {
                logger.info("User logged in successfully: {}", loginBody.getUsername());
                response.put("jwt", jwt);
                response.put("message", "User logged in successfully!");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Invalid login credentials for user: {}", loginBody.getUsername());
                response.put("success", false);
                response.put("message", "Invalid login credentials.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (UserNotVerifiedException ex) {
            logger.warn("User not verified: {}", loginBody.getUsername());
            response.put("success", false);
            String reason = "USER_NOT_VERIFIED";
            if (ex.istNewEmailSent()) {
                reason += "_EMAIL_RESENT";
            }
            response.put("message", reason);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (EmailFailureException ex) {
            logger.error("Failed to send verification email for user: {}", loginBody.getUsername(), ex);
            response.put("success", false);
            response.put("message", "Failed to send verification email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception ex) {
            logger.error("An error occurred during login: {}", ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "An error occurred: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();

        if (userService.verifyUser(token)) {
            response.put("success", true);
            response.put("message", "Email verified successfully.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired verification token.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.forgotPassword(email);
            logger.info("Password reset email sent successfully to: {}", email);
            response.put("success", true);
            response.put("message", "Password reset email sent successfully.");
            return ResponseEntity.ok(response);
        } catch (EmailNotFoundException ex) {
            logger.warn("Email address not found for password reset: {}", email);
            response.put("success", false);
            response.put("message", "Email address not found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (EmailFailureException ex) {
            logger.error("Failed to send password reset email to: {}", email, ex);
            response.put("success", false);
            response.put("message", "Failed to send password reset email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody PasswordResetBody body) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isResetSuccessful = userService.resetPassword(body);
            if (isResetSuccessful) {
                logger.info("Password reset successfully for token: {}", body.getToken());
                response.put("success", true);
                response.put("message", "Password reset successfully.");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Password reset failed for token: {}", body.getToken());
                response.put("success", false);
                response.put("message", "Password reset failed. Please check the token or the new password.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        } catch (Exception ex) {
            logger.error("Failed to reset password for token: {}", body.getToken(), ex);
            response.put("success", false);
            response.put("message", "Failed to reset password. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
