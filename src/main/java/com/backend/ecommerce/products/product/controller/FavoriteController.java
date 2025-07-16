package com.backend.ecommerce.products.product.controller;

import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.service.FavoriteService;
import com.backend.ecommerce.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{productId}")
    public ResponseEntity<String> addProductToFavorites(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        favoriteService.addProductToFavorites(user, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body("Product added to favorites.");
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeProductFromFavorites(@AuthenticationPrincipal User user, @PathVariable Long productId) {
        favoriteService.removeProductFromFavorites(user, productId);
        return ResponseEntity.status(HttpStatus.OK).body("Product removed from favorites.");
    }

    @GetMapping
    public ResponseEntity<List<Product>> getUserFavorites(@AuthenticationPrincipal User user) {
        List<Product> favorites = favoriteService.getUserFavorites(user);
        return ResponseEntity.ok(favorites);
    }
}
