package com.bookstore.controller;

import com.bookstore.dto.AuthorDto;
import com.bookstore.dto.BookDetailResponseDto;
import com.bookstore.dto.BookDto;
import com.bookstore.dto.CategoryDto;
import com.bookstore.entity.Branch;
import com.bookstore.service.AuthorService;
import com.bookstore.service.BranchService;
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
    private final AuthorService authorService;
    private final BranchService branchService;

    public HomeController(
            MasterDataService masterDataService,
            CategoryService categoryService,
            AuthorService authorService,
            BranchService branchService
    ) {
        this.masterDataService = masterDataService;
        this.categoryService = categoryService;
        this.authorService = authorService;
        this.branchService = branchService;
    }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("pageTitle", "Book4Life - Hiệu Sách Cũ Trực Tuyến & Bền Vững Hàng Đầu");
        try {
            List<CategoryDto> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            Pageable pageable = PageRequest.of(0, 8);
            Page<BookDto> featuredBooks = masterDataService.searchBooks(null, null, null, pageable);
            model.addAttribute("featuredBooks", featuredBooks.getContent());
        } catch (Exception ignored) {}
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
        List<AuthorDto> authors = authorService.getAllAuthors();

        model.addAttribute("pageTitle", "Kho Sách Cũ Tuyển Chọn - Book4Life");
        model.addAttribute("bookPage", bookPage);
        model.addAttribute("categories", categories);
        model.addAttribute("authors", authors);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedAuthorId", authorId);

        return "books/catalog";
    }

    @GetMapping("/categories")
    public String viewCategories(
            @RequestParam(name = "id", required = false) Long categoryId,
            Model model
    ) {
        return "redirect:/books" + (categoryId != null ? "?categoryId=" + categoryId : "");
    }

    @GetMapping("/books/{id}")
    public String bookDetail(@PathVariable("id") Long id, Model model) {
        try {
            BookDetailResponseDto detail = masterDataService.getBookDetail(id);
            model.addAttribute("book", detail);
            model.addAttribute("pageTitle", detail.getTitle() + " - Book4Life");
            
            // Related books from the same category
            if (detail.getCategoryId() != null) {
                Page<BookDto> related = masterDataService.searchBooks(null, detail.getCategoryId(), null, PageRequest.of(0, 4));
                model.addAttribute("relatedBooks", related.getContent());
            }
            return "books/detail";
        } catch (Exception e) {
            return "redirect:/books";
        }
    }

    @GetMapping("/authors")
    public String listAuthors(
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model
    ) {
        return handleAuthorView(null, keyword, model);
    }

    @GetMapping("/authors/{id}")
    public String authorDetail(
            @PathVariable("id") Long authorId,
            @RequestParam(name = "keyword", required = false) String keyword,
            Model model
    ) {
        return handleAuthorView(authorId, keyword, model);
    }

    private String handleAuthorView(Long authorId, String keyword, Model model) {
        List<AuthorDto> authors = authorService.getAllAuthors();
        model.addAttribute("authors", authors);

        AuthorDto featuredAuthor = null;
        if (authorId != null) {
            try {
                featuredAuthor = authorService.getAuthorById(authorId);
            } catch (Exception ignored) {}
        }
        if (featuredAuthor == null && !authors.isEmpty()) {
            featuredAuthor = authors.get(0);
        }
        model.addAttribute("featuredAuthor", featuredAuthor);

        if (featuredAuthor != null) {
            Page<BookDto> authorBooks = masterDataService.searchBooks(null, null, featuredAuthor.getId(), PageRequest.of(0, 12));
            model.addAttribute("authorBooks", authorBooks.getContent());
        }

        model.addAttribute("pageTitle", (featuredAuthor != null ? featuredAuthor.getName() + " - " : "") + "Tác Giả & Tác Phẩm - Book4Life");
        return "authors/list";
    }

    @GetMapping({"/blog", "/blog/{id}"})
    public String blog(
            @PathVariable(name = "id", required = false) Long articleId,
            Model model
    ) {
        model.addAttribute("pageTitle", "Góc Đọc Sách & Blog Văn Hóa Sách Cũ - Book4Life");
        try {
            Page<BookDto> recommendedBooks = masterDataService.searchBooks(null, null, null, PageRequest.of(0, 4));
            model.addAttribute("recommendedBooks", recommendedBooks.getContent());
        } catch (Exception ignored) {}
        return "blog/index";
    }

    @GetMapping({"/about", "/contact", "/recycle", "/thu-mua-sach"})
    public String aboutAndBuyback(Model model) {
        model.addAttribute("pageTitle", "Về Chúng Tôi & Điểm Thu Mua Sách Cũ - Book4Life");
        try {
            List<Branch> branches = branchService.getAllActiveBranches();
            model.addAttribute("branches", branches);
        } catch (Exception ignored) {}
        return "about/index";
    }
}
