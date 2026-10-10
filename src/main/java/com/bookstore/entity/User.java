package com.bookstore.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public User() {
    }

    public User(Long id, String email, String password, String fullName, String phone,
                String avatarUrl, Role role, Branch branch, Boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.branch = branch;
        this.isActive = (isActive != null) ? isActive : true;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Branch getBranch() {
        return branch;
    }

    public void setBranch(Branch branch) {
        this.branch = branch;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private String email;
        private String password;
        private String fullName;
        private String phone;
        private String avatarUrl;
        private Role role;
        private Branch branch;
        private Boolean isActive = true;
        private LocalDateTime createdAt = LocalDateTime.now();

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder password(String password) {
            this.password = password;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder avatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder branch(Branch branch) {
            this.branch = branch;
            return this;
        }

        public Builder isActive(Boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public User build() {
            return new User(id, email, password, fullName, phone, avatarUrl, role, branch, isActive, createdAt);
        }
    }
}
