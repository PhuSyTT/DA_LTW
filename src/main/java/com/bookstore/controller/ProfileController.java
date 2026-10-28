package com.bookstore.controller;

import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/profile")
    public String profilePage(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/profile";
        }

        User user = userRepository.findById(userDetails.getId()).orElse(userDetails.getUser());
        model.addAttribute("user", user);
        model.addAttribute("pageTitle", "Thông Tin Tài Khoản & Bảo Mật - Book4Life");
        return "customer/profile";
    }
}
