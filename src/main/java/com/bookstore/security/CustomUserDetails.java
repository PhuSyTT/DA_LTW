package com.bookstore.security;

import com.bookstore.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullName;
    private final String phone;
    private final String avatarUrl;
    private final String roleName;
    private final Long branchId;
    private final String branchName;
    private final boolean isActive;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullName = user.getFullName();
        this.phone = user.getPhone();
        this.avatarUrl = user.getAvatarUrl();
        this.isActive = Boolean.TRUE.equals(user.getIsActive());

        String rawRole = (user.getRole() != null) ? user.getRole().getRoleName() : "CUSTOMER";
        this.roleName = rawRole;

        if (user.getBranch() != null) {
            this.branchId = user.getBranch().getId();
            this.branchName = user.getBranch().getBranchName();
        } else {
            this.branchId = null;
            this.branchName = null;
        }

        List<GrantedAuthority> auths = new ArrayList<>();
        // Add authority with ROLE_ prefix
        String formattedRole = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;
        auths.add(new SimpleGrantedAuthority(formattedRole));
        // Also add direct name for convenience
        if (!formattedRole.equals(rawRole)) {
            auths.add(new SimpleGrantedAuthority(rawRole));
        }
        this.authorities = auths;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getRoleName() {
        return roleName;
    }

    public Long getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive;
    }
}
