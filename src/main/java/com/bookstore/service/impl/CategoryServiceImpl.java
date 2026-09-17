package com.bookstore.service.impl;

import com.bookstore.dto.CategoryDto;
import com.bookstore.entity.Category;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDto> getCategoryHierarchy() {
        List<Category> rootCategories = categoryRepository.findByParentIsNullOrderByNameAsc();
        return rootCategories.stream()
                .map(this::mapToHierarchyDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long id) {
        Category category = getCategoryEntityById(id);
        return mapToDto(category);
    }

    @Override
    public Category getCategoryEntityById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại với ID: " + id));
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByNameIgnoreCase(categoryDto.getName().trim())) {
            throw new IllegalArgumentException("Thể loại '" + categoryDto.getName() + "' đã tồn tại trong hệ thống");
        }
        Category parent = null;
        if (categoryDto.getParentId() != null) {
            parent = getCategoryEntityById(categoryDto.getParentId());
        }

        Category category = Category.builder()
                .name(categoryDto.getName().trim())
                .parent(parent)
                .build();
        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long id, CategoryDto categoryDto) {
        Category category = getCategoryEntityById(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(categoryDto.getName().trim(), id)) {
            throw new IllegalArgumentException("Tên thể loại '" + categoryDto.getName() + "' đã được sử dụng bởi danh mục khác");
        }

        Category parent = null;
        if (categoryDto.getParentId() != null) {
            if (categoryDto.getParentId().equals(id)) {
                throw new IllegalArgumentException("Thể loại cha không thể là chính nó!");
            }
            parent = getCategoryEntityById(categoryDto.getParentId());
        }

        category.setName(categoryDto.getName().trim());
        category.setParent(parent);
        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getCategoryEntityById(id);
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            throw new IllegalStateException("Không thể xóa thể loại cha '" + category.getName() + "' vì đang có danh mục con trực thuộc!");
        }
        if (category.getBooks() != null && !category.getBooks().isEmpty()) {
            throw new IllegalStateException("Không thể xóa thể loại '" + category.getName() + "' vì đang có " + category.getBooks().size() + " đầu sách liên kết!");
        }
        categoryRepository.delete(category);
    }

    private CategoryDto mapToDto(Category category) {
        long bookCount = (category.getBooks() != null) ? category.getBooks().size() : 0;
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .parentId((category.getParent() != null) ? category.getParent().getId() : null)
                .parentName((category.getParent() != null) ? category.getParent().getName() : null)
                .bookCount(bookCount)
                .build();
    }

    private CategoryDto mapToHierarchyDto(Category category) {
        CategoryDto dto = mapToDto(category);
        if (category.getSubCategories() != null && !category.getSubCategories().isEmpty()) {
            dto.setSubCategories(category.getSubCategories().stream()
                    .map(this::mapToHierarchyDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setSubCategories(new ArrayList<>());
        }
        return dto;
    }
}
