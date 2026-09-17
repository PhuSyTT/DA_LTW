package com.bookstore.controller.admin;

import com.bookstore.dto.CategoryDto;
import com.bookstore.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;

    public AdminCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        List<CategoryDto> hierarchy = categoryService.getCategoryHierarchy();
        List<CategoryDto> allFlat = categoryService.getAllCategories();

        model.addAttribute("categoryHierarchy", hierarchy);
        model.addAttribute("allCategories", allFlat);
        model.addAttribute("newCategory", new CategoryDto());
        model.addAttribute("activeMenu", "categories");

        return "admin/categories/list";
    }

    @PostMapping("/create")
    public String createCategory(
            @Valid @ModelAttribute("newCategory") CategoryDto categoryDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên thể loại không hợp lệ!");
            return "redirect:/admin/categories";
        }

        try {
            categoryService.createCategory(categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm thể loại '" + categoryDto.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/edit/{id}")
    public String updateCategory(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("categoryDto") CategoryDto categoryDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu thể loại không hợp lệ!");
            return "redirect:/admin/categories";
        }

        try {
            categoryService.updateCategory(id, categoryDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thể loại thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thể loại thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}
