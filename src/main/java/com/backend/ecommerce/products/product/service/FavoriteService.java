package com.backend.ecommerce.products.product.service;

import com.backend.ecommerce.products.product.dao.FavoriteDAO;
import com.backend.ecommerce.products.product.model.Favorite;
import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.products.product.dao.ProductDAO;
import com.backend.ecommerce.user.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    private final FavoriteDAO favoriteDAO;
    private final ProductDAO productDAO;

    public FavoriteService(FavoriteDAO favoriteDAO, ProductDAO productDAO) {
        this.favoriteDAO = favoriteDAO;
        this.productDAO = productDAO;
    }

    public Favorite addProductToFavorites(User user, Long productId) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (favoriteDAO.existsByUserAndProduct(user, product)) {
            throw new IllegalStateException("Product already in favorites");
        }

        Favorite favorite = new Favorite(user, product);
        return favoriteDAO.save(favorite);
    }

    public void removeProductFromFavorites(User user, Long productId) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Favorite favorite = favoriteDAO.findByUserAndProduct(user, product)
                .orElseThrow(() -> new IllegalArgumentException("Favorite not found"));

        favoriteDAO.delete(favorite);
    }

    public List<Product> getUserFavorites(User user) {
        List<Favorite> favorites = favoriteDAO.findByUser(user);
        return favorites.stream()
                .map(Favorite::getProduct)
                .collect(Collectors.toList());
    }
}
