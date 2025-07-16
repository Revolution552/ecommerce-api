package com.backend.ecommerce.products.product.dao;

import com.backend.ecommerce.products.product.model.Favorite;
import com.backend.ecommerce.products.product.model.Product;
import com.backend.ecommerce.user.model.User;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteDAO extends ListCrudRepository<Favorite, Long> {

    List<Favorite> findByUser(User user);

    Optional<Favorite> findByUserAndProduct(User user, Product product);

    boolean existsByUserAndProduct(User user, Product product);

    void deleteByUserAndProduct(User user, Product product);
}
