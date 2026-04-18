package com.backend.ecommerce.user.payload;

public record UserResponseDto(
        Long id,
        String firstName,
        String surname,
        String email,
        boolean emailVerified
) {}