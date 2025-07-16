package com.backend.ecommerce.user.controller;

import com.backend.ecommerce.user.dto.LoginRequest;
import com.backend.ecommerce.user.dto.RegisterRequest;
import com.backend.ecommerce.user.dto.ResetPasswordRequest;
import com.backend.ecommerce.user.exception.EmailFailureException;
import com.backend.ecommerce.user.exception.EmailNotFoundException;
import com.backend.ecommerce.user.exception.UserAlreadyExistsException;
import com.backend.ecommerce.user.exception.UserNotVerifiedException;
import com.backend.ecommerce.user.repository.UserRepository;
import com.backend.ecommerce.user.service.JWTService;
import com.backend.ecommerce.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserRepository userRepository;
    private final JWTService jwtService;

    public UserController(UserService userService, UserRepository userRepository, JWTService jwtService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@Valid @RequestBody RegisterRequest request) {
        Map<String, String> response = new HashMap<>();
        try {
            userService.registerUser(request);
            logger.info("User registered successfully: {}", request.getEmail());
            response.put("message", "User registered successfully!");
            response.put("status", "OK");
            return ResponseEntity.ok(response);

        } catch (UserAlreadyExistsException ex) {
            logger.warn("User already exists: {}", request.getEmail());
            response.put("message", "User already exists!");
            response.put("status", "CONFLICT");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (EmailFailureException ex) {
            logger.error("Failed to send confirmation email to: {}", request.getEmail(), ex);
            response.put("message", "Failed to send confirmation email!");
            response.put("status", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception ex) {
            logger.error("Unexpected error during registration: {}", ex.getMessage(), ex);
            response.put("message", "Unexpected error: " + ex.getMessage());
            response.put("status", "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginUser(@Valid @RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        try {
            String jwt = userService.loginUser(request);
            if (jwt != null) {
                logger.info("User logged in: {}", request.getEmail());
                response.put("jwt", jwt);
                response.put("message", "Login successful!");
                response.put("success", true);
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Invalid credentials for: {}", request.getEmail());
                response.put("success", false);
                response.put("message", "Invalid email or password.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (UserNotVerifiedException ex) {
            logger.warn("User not verified: {}", request.getEmail(), ex);
            response.put("success", false);
            response.put("message", ex.getMessage()); // includes info about email re-sent
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);

        } catch (EmailFailureException ex) {
            logger.error("Failed to send verification email to: {}", request.getEmail(), ex);
            response.put("success", false);
            response.put("message", "Could not send verification email. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (LazyInitializationException ex) {
            logger.error("Lazy initialization error: {}", ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "Internal error. Please try again later.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (Exception ex) {
            logger.error("Unexpected login error: {}", ex.getMessage(), ex);
            response.put("success", false);
            response.put("message", "Unexpected error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        Map<String, Object> response = new HashMap<>();

        if (token == null || token.isBlank()) {
            response.put("success", false);
            response.put("message", "Token cannot be empty.");
            return ResponseEntity.badRequest().body(response);
        }

        if (userService.verifyUser(token)) {
            response.put("success", true);
            response.put("message", "Email verified.");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid or expired verification token.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        String tokenHeader = request.getHeader("Authorization");

        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            String email = jwtService.getEmail(token);

            logger.info("Logout requested by user: {}", email);
            jwtService.invalidateToken(token); // Optional - implement in JWTService if needed
            logger.info("Token invalidated for user: {}", email);

            response.put("success", true);
            response.put("message", "Logged out successfully.");
            return ResponseEntity.ok(response);
        } else {
            logger.warn("Missing or invalid Authorization header.");
            response.put("success", false);
            response.put("message", "Authorization token not found.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/forgot")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();

        try {
            userService.forgotPassword(email);
            logger.info("Password reset email sent to: {}", email);
            response.put("success", true);
            response.put("message", "Password reset email sent.");
            return ResponseEntity.ok(response);

        } catch (EmailNotFoundException ex) {
            logger.warn("Email not found: {}", email);
            response.put("success", false);
            response.put("message", "Email not found.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (EmailFailureException ex) {
            logger.error("Password reset email failure: {}", email, ex);
            response.put("success", false);
            response.put("message", "Failed to send password reset email.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean result = userService.resetPasswordWithToken(
                    request.getToken(),
                    request.getNewPassword(),
                    request.getConfirmPassword()
            );

            if (result) {
                logger.info("Password reset for token: {}", request.getToken());
                response.put("success", true);
                response.put("message", "Password reset successful.");
                return ResponseEntity.ok(response);
            } else {
                // Should not happen if your service throws for all errors,
                // but handled just in case.
                logger.warn("Password reset failed (unexpected false) for token: {}", request.getToken());
                response.put("success", false);
                response.put("message", "Password reset failed.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (IllegalArgumentException ex) {
            logger.warn("Password reset failed for token: {} - {}", request.getToken(), ex.getMessage());
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (Exception ex) {
            logger.error("Password reset error for token: {}", request.getToken(), ex);
            response.put("success", false);
            response.put("message", "Unexpected error: " + ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
