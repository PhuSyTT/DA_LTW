package com.bookstore.controller.admin;

import com.bookstore.dto.BookDetailResponseDto;
import com.bookstore.dto.BookDto;
import com.bookstore.service.AuthorService;
import com.bookstore.service.CategoryService;
import com.bookstore.service.MasterDataService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/books")
public class AdminMasterDataController {

    private final MasterDataService masterDataService;
    private final AuthorService authorService;
    private final CategoryService categoryService;

    public AdminMasterDataController(MasterDataService masterDataService,
                                     AuthorService authorService,
                                     CategoryService categoryService) {
        this.masterDataService = masterDataService;
        this.authorService = authorService;
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listBooks(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "categoryId", required = false) Long categoryId,
            @RequestParam(name = "authorId", required = false) Long authorId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(name = "sortDir", defaultValue = "desc") String sortDir,
            Model model
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<BookDto> bookPage = masterDataService.searchBooks(keyword, categoryId, authorId, pageable);

        model.addAttribute("bookPage", bookPage);
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("totalElements", bookPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("authorId", authorId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("totalBooks", masterDataService.getTotalBooksCount());
        model.addAttribute("totalInventory", masterDataService.getTotalInventoryCount());

        model.addAttribute("activeMenu", "books");
        return "admin/books/list";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("bookDto")) {
            model.addAttribute("bookDto", new BookDto());
        }
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("authors", authorService.getAllAuthors());
        model.addAttribute("isEdit", false);
        model.addAttribute("activeMenu", "books");
        return "admin/books/form";
    }

    @PostMapping("/create")
    public String createBook(
            @Valid @ModelAttribute("bookDto") BookDto bookDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!masterDataService.isIsbnAvailable(bookDto.getIsbn(), null)) {
            bindingResult.rejectValue("isbn", "duplicate.isbn", "Mã ISBN '" + bookDto.getIsbn() + "' đã tồn tại trong Master Data!");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("authors", authorService.getAllAuthors());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", "books");
            return "admin/books/form";
        }

        try {
            BookDto created = masterDataService.createBook(bookDto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Thêm mới đầu sách gốc '" + created.getTitle() + "' (ISBN: " + created.getIsbn() + ") thành công!");
            return "redirect:/admin/books";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi lưu đầu sách: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("authors", authorService.getAllAuthors());
            model.addAttribute("isEdit", false);
            model.addAttribute("activeMenu", "books");
            return "admin/books/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        try {
            BookDto bookDto = masterDataService.getBookDtoById(id);
            model.addAttribute("bookDto", bookDto);
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("authors", authorService.getAllAuthors());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "books");
            return "admin/books/form";
        } catch (Exception e) {
            return "redirect:/admin/books";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateBook(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("bookDto") BookDto bookDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (!masterDataService.isIsbnAvailable(bookDto.getIsbn(), id)) {
            bindingResult.rejectValue("isbn", "duplicate.isbn", "Mã ISBN '" + bookDto.getIsbn() + "' đã thuộc về đầu sách khác!");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("authors", authorService.getAllAuthors());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "books");
            return "admin/books/form";
        }

        try {
            BookDto updated = masterDataService.updateBook(id, bookDto);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Cập nhật đầu sách '" + updated.getTitle() + "' thành công!");
            return "redirect:/admin/books";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Lỗi khi cập nhật: " + e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("authors", authorService.getAllAuthors());
            model.addAttribute("isEdit", true);
            model.addAttribute("activeMenu", "books");
            return "admin/books/form";
        }
    }

    @GetMapping("/detail/{id}")
    public String viewBookDetail(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            BookDetailResponseDto detail = masterDataService.getBookDetail(id);
            model.addAttribute("book", detail);
            model.addAttribute("activeMenu", "books");
            return "admin/books/detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đầu sách với ID: " + id);
            return "redirect:/admin/books";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            masterDataService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa đầu sách thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa đầu sách: " + e.getMessage());
        }
        return "redirect:/admin/books";
    }
}
