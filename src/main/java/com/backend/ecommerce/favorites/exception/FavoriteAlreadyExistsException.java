// src/main/java/com/backend/ecommerce/favorites/exception/FavoriteAlreadyExistsException.java
package com.backend.ecommerce.favorites.exception;

public class FavoriteAlreadyExistsException extends RuntimeException {
    public FavoriteAlreadyExistsException(String message) {
        super(message);
    }
}