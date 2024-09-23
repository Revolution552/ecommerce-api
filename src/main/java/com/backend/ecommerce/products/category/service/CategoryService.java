package com.backend.ecommerce.products.category.service;

import com.backend.ecommerce.products.category.dao.CategoryDAO;
import com.backend.ecommerce.products.category.model.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService(CategoryDAO categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<Category> getAllCategories() {
        return categoryDAO.findAll();
    }

    public Category createCategory(Category category) {
        return categoryDAO.save(category);
    }

    public Category getCategoryById(Long id) {
        return categoryDAO.findById(id).orElse(null);
    }
}
