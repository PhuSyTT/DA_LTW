package com.bookstore.controller.admin;

import com.bookstore.dto.UserDto;
import com.bookstore.entity.Branch;
import com.bookstore.repository.BranchRepository;
import com.bookstore.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final BranchRepository branchRepository;

    public AdminUserController(UserService userService, BranchRepository branchRepository) {
        this.userService = userService;
        this.branchRepository = branchRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<UserDto> users = userService.getAllUsers();
        List<Branch> branches = branchRepository.findByIsActiveTrueOrderByIdAsc();

        model.addAttribute("pageTitle", "Quản Lý Tài Khoản & Phân Quyền");
        model.addAttribute("pageSubtitle", "Quản lý 5 vai trò nhân sự & độc giả theo chuẩn tài liệu SRS");
        model.addAttribute("activeMenu", "users");
        model.addAttribute("users", users);
        model.addAttribute("roles", userService.getAllRoles());
        model.addAttribute("branches", branches);
        model.addAttribute("newUser", new UserDto());

        return "admin/users/list";
    }

    @PostMapping("/create")
    public String createUser(@ModelAttribute UserDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/update")
    public String updateUser(@PathVariable Long id, @ModelAttribute UserDto userDto, RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, userDto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle")
    public String toggleStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thay đổi trạng thái kích hoạt tài khoản!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa tài khoản thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
