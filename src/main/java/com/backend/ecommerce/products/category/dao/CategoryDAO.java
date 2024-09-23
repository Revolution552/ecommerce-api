package com.backend.ecommerce.products.category.dao;

import com.backend.ecommerce.products.category.model.Category;
import org.springframework.data.repository.ListCrudRepository;

public interface CategoryDAO extends ListCrudRepository<Category, Long> {

    Category findByName(String name);
}