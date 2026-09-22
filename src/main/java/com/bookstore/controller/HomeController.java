package com.bookstore.controller;

import com.bookstore.dto.BookDetailResponseDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.CategoryDto;
import com.bookstore.service.CategoryService;
import com.bookstore.service.MasterDataService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final MasterDataService masterDataService;
    private final CategoryService categoryService;

    public HomeController(MasterDataService masterDataService, CategoryService categoryService) {
        this.masterDataService = masterDataService;
        this.categoryService = categoryService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "OldBooks - Tìm Những Cuốn Sách Đã Có Một Câu Chuyện");
        return "welcome";
    }

    @GetMapping("/books")
    public String listPublicBooks(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "authorId", required = false) Long authorId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "12") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BookDto> bookPage = masterDataService.searchBooks(keyword, categoryId, authorId, pageable);
        List<CategoryDto> categories = categoryService.getAllCategories();

        model.addAttribute("pageTitle", "Kho Sách Cũ Tuyển Chọn - OldBooks");
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("categories", categories);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedAuthorId", authorId);

        return "books/catalog";
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        try {
            BookDetailResponseDto detail = masterDataService.getBookDetail(id);
            model.addAttribute("book", detail);
            model.addAttribute("pageTitle", detail.getTitle() + " - OldBooks");
            return "books/detail";
        } catch (Exception e) {
            return "redirect:/books";
        }
    }
}
