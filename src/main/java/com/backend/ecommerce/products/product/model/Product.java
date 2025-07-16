package com.backend.ecommerce.products.product.model;

import com.backend.ecommerce.products.category.model.Category;
import com.backend.ecommerce.products.shop.model.Shop;
import com.backend.ecommerce.user.model.User;
import com.backend.ecommerce.user.repository.UserRepository;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private int popularity;

    @Column(nullable = false)
    private double rating;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who posted the product

    // Adding a method to get the UserDTO instead of the entire User object
//    public UserRepository getUserRepository() {
//        return new UserRepository(user.getId(), user.getEmail()) {
//        };
//    }

    public Product(String name, Double price, String description, Category category, int popularity, double rating, String imageUrl, User user) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.category = category;
        this.popularity = popularity;
        this.rating = rating;
        this.imageUrl = imageUrl;
        this.user = user;
    }
}
