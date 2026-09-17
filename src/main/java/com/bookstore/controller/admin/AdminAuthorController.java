package com.bookstore.controller.admin;

import com.bookstore.dto.AuthorDto;
import com.bookstore.service.AuthorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/authors")
public class AdminAuthorController {

    private final AuthorService authorService;

    public AdminAuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public String listAuthors(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuthorDto> authorPage = authorService.getAuthors(keyword, pageable);

        model.addAttribute("authorPage", authorPage);
        model.addAttribute("authors", authorPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", authorPage.getTotalPages());
        model.addAttribute("totalElements", authorPage.getTotalElements());
        model.addAttribute("keyword", keyword);
        model.addAttribute("newAuthor", new AuthorDto());
        model.addAttribute("activeMenu", "authors");

        return "admin/authors/list";
    }

    @PostMapping("/create")
    public String createAuthor(
            @Valid @ModelAttribute("newAuthor") AuthorDto authorDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Tên tác giả không hợp lệ!");
            return "redirect:/admin/authors";
        }

        try {
            authorService.createAuthor(authorDto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm tác giả '" + authorDto.getName() + "' thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/authors";
    }

    @PostMapping("/edit/{id}")
    public String updateAuthor(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("authorDto") AuthorDto authorDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Dữ liệu không hợp lệ!");
            return "redirect:/admin/authors";
        }

        try {
            authorService.updateAuthor(id, authorDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tác giả thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/authors";
    }

    @PostMapping("/delete/{id}")
    public String deleteAuthor(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            authorService.deleteAuthor(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa tác giả thành công!");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/authors";
    }
}
