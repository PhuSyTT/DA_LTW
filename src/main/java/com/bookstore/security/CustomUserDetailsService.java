package com.bookstore.security;

import com.bookstore.entity.User;
import com.bookstore.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String input) throws UsernameNotFoundException {
        if (input == null || input.trim().isEmpty()) {
            throw new UsernameNotFoundException("Tên đăng nhập không được để trống!");
        }
        String cleanInput = input.trim().toLowerCase();

        // Support convenient test aliases
        String resolvedEmail = cleanInput;
        if ("admin".equals(cleanInput)) {
            resolvedEmail = "admin@oldbooks.vn";
        } else if ("user1".equals(cleanInput) || "customer".equals(cleanInput) || "user".equals(cleanInput)) {
            resolvedEmail = "customer@gmail.com";
        } else if ("manager".equals(cleanInput)) {
            resolvedEmail = "manager.hn@oldbooks.vn";
        } else if ("warehouse".equals(cleanInput) || "appraiser".equals(cleanInput)) {
            resolvedEmail = "warehouse@oldbooks.vn";
        } else if ("cashier".equals(cleanInput) || "staff".equals(cleanInput)) {
            resolvedEmail = "cashier@oldbooks.vn";
        }

        final String finalEmail = resolvedEmail;
        User user = userRepository.findByEmail(finalEmail)
                .or(() -> userRepository.findByEmail(cleanInput))
                .or(() -> userRepository.findByEmail(input.trim()))
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản với email/tên đăng nhập: " + input));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UsernameNotFoundException("Tài khoản của bạn đã bị khóa hoặc tạm dừng hoạt động!");
        }

        return new CustomUserDetails(user);
    }
}
