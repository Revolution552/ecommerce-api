package com.backend.ecommerce.products.category.dao;

import com.backend.ecommerce.products.category.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // IMPORTANT: Ensure Optional is imported

@Repository
public interface CategoryDAO extends JpaRepository<Category, Long> {
    // This method should return Optional, just like JpaRepository's findById
    Optional<Category> findByName(String name);

    // This method returns a List, which is fine
    List<Category> findByNameContainingIgnoreCase(String name);

    // Note: findById(Long id) is inherited from JpaRepository and correctly returns Optional<Category>
}
