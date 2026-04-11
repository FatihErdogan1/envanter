package org.example.controller;

import org.example.model.entity.Category;
import org.example.service.CategoryService;

import java.util.List;

public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController() {
        this.categoryService = new CategoryService();
    }

    public List<Category> getAllCategories() {
        return categoryService.getAllCategories();
    }

    public boolean addCategory(String name, String description) throws IllegalArgumentException {
        Category category = new Category(0, name, description);
        return categoryService.addCategory(category);
    }

    public boolean updateCategory(int id, String name, String description) {
        Category category = new Category(id, name, description);
        return categoryService.updateCategory(category);
    }

    // CategoryController.java
    public boolean deleteCategory(int id) throws IllegalStateException {
        // Service'den gelen IllegalStateException arayüze kadar iletilir
        return categoryService.deleteCategory(id);
    }
}
