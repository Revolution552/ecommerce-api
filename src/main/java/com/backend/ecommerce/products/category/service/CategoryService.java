package com.backend.ecommerce.products.category.service;

import com.backend.ecommerce.products.category.dao.CategoryDAO;
import com.backend.ecommerce.products.category.model.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryDAO categoryDAO;

    @Autowired
    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryDAO.findById(id);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryDAO.findByName(name);
    }

    /**
     * Searches for categories whose names contain the given keyword, ignoring case.
     * This method is specifically added to support frontend search functionality.
     *
     * @param keyword The string to search for within category names.
     * @return A list of matching Category objects.
     */
    public List<Category> searchCategoriesByName(String keyword) {
        return categoryDAO.findByNameContainingIgnoreCase(keyword);
    }
}
