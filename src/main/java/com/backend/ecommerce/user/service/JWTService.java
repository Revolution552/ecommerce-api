package com.backend.ecommerce.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.backend.ecommerce.user.model.User;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JWTService {

    @Value("${jwt.algorithm.key}")
    private String algorithmKey;
    @Value("${jwt.issuer}")
    private String issuer;
    @Value("${jwt.expiryInSeconds}")
    private int expiryInSeconds;

    private Algorithm algorithm;
    private static final String EMAIL_KEY = "EMAIL";
    private static final String ROLES_KEY = "ROLES";  // New constant for roles claim
    private static final String RESET_PASSWORD_EMAIL_KEY = "RESET_PASSWORD_EMAIL";

    private final Set<String> invalidatedTokens = new HashSet<>();

    @PostConstruct
    public void postConstruct() {
        this.algorithm = Algorithm.HMAC256(algorithmKey);
    }

    // Updated to include roles in JWT
    public String generateJWT(User user) {
        return JWT.create()
                .withClaim(EMAIL_KEY, user.getEmail())
                .withClaim(ROLES_KEY, user.getAuthorities().stream()  // Add roles to token
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .withExpiresAt(new Date(System.currentTimeMillis() + (1000L * expiryInSeconds)))
                .withIssuer(issuer)
                .sign(algorithm);
    }

    // New method to extract roles from token
    public Set<String> getRoles(String token) {
        DecodedJWT jwt = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return new HashSet<>(jwt.getClaim(ROLES_KEY).asList(String.class));
    }

    // 6-digit password reset code (unchanged)
    public String generatePasswordResetToken(User user) {
        int code = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(code);
    }

    // Extract email from reset token (unchanged)
    public String getResetPasswordEmail(String token) {
        DecodedJWT jwt = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return jwt.getClaim(RESET_PASSWORD_EMAIL_KEY).asString();
    }

    // Extract email from login token (unchanged)
    public String getEmail(String token) {
        DecodedJWT jwt = JWT.require(algorithm)
                .withIssuer(issuer)
                .build()
                .verify(token);
        return jwt.getClaim(EMAIL_KEY).asString();
    }

    // Token invalidation (unchanged)
    public void invalidateToken(String token) {
        invalidatedTokens.add(token);
    }

    public boolean isTokenInvalidated(String token) {
        return invalidatedTokens.contains(token);
    }
}