package com.bookstore.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            // Already logged in, redirect
            boolean isStaff = auth.getAuthorities().stream().anyMatch(a -> {
                String r = a.getAuthority().toUpperCase();
                return r.contains("ADMIN") || r.contains("BRANCH_MANAGER") || r.contains("WAREHOUSE_STAFF") || r.contains("CASHIER");
            });
            return isStaff ? "redirect:/admin/books" : "redirect:/";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không chính xác, hoặc tài khoản đã bị khóa!");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất khỏi hệ thống thành công!");
        }

        model.addAttribute("pageTitle", "Đăng Nhập Hệ Thống - Book4Life");
        return "auth/login";
    }
}
