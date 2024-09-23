package com.backend.ecommerce.products.product.dao;

import com.backend.ecommerce.products.category.model.Category;
import com.backend.ecommerce.products.product.model.Product;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductDAO extends ListCrudRepository<Product, Long> {
    Category findByName(String name);
}