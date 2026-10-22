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
        if ("admin".equals(cleanInput) || "admin@oldbooks.vn".equals(cleanInput)) {
            resolvedEmail = "admin.book4life@gmail.com";
        } else if ("user1".equals(cleanInput) || "customer".equals(cleanInput) || "user".equals(cleanInput) || "customer@gmail.com".equals(cleanInput)) {
            resolvedEmail = "user.book4life@gmail.com";
        } else if ("manager".equals(cleanInput) || "manage".equals(cleanInput) || "manager.hn@oldbooks.vn".equals(cleanInput)) {
            resolvedEmail = "manage.book4life@gmail.com";
        } else if ("warehouse".equals(cleanInput) || "appraiser".equals(cleanInput) || "warehouse@oldbooks.vn".equals(cleanInput)) {
            resolvedEmail = "warehouse.book4life@gmail.com";
        } else if ("cashier".equals(cleanInput) || "staff".equals(cleanInput) || "cashier@oldbooks.vn".equals(cleanInput)) {
            resolvedEmail = "cashier.book4life@gmail.com";
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
