package com.bookstore.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        boolean isStaffOrAdmin = authorities.stream().anyMatch(auth -> {
            String role = auth.getAuthority().toUpperCase();
            return role.contains("ADMIN") ||
                   role.contains("BRANCH_MANAGER") ||
                   role.contains("WAREHOUSE_STAFF") ||
                   role.contains("CASHIER");
        });

        if (isStaffOrAdmin) {
            response.sendRedirect(request.getContextPath() + "/admin/books");
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}
