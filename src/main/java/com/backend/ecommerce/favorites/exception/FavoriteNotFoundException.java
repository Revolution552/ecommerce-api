// src/main/java/com/backend/ecommerce/favorites/exception/FavoriteNotFoundException.java
package com.backend.ecommerce.favorites.exception;

public class FavoriteNotFoundException extends RuntimeException {
    public FavoriteNotFoundException(String message) {
        super(message);
    }
}