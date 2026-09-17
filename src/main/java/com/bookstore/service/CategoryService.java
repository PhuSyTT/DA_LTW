package com.bookstore.service;

import com.bookstore.dto.CategoryDto;
import com.bookstore.entity.Category;

import java.util.List;

public interface CategoryService {
    List<CategoryDto> getAllCategories();
    List<CategoryDto> getCategoryHierarchy();
    CategoryDto getCategoryById(Long id);
    Category getCategoryEntityById(Long id);
    CategoryDto createCategory(CategoryDto categoryDto);
    CategoryDto updateCategory(Long id, CategoryDto categoryDto);
    void deleteCategory(Long id);
}
