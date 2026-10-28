package com.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordResetInitRequestDto {

    @NotBlank(message = "Địa chỉ email không được để trống!")
    @Email(message = "Định dạng email không hợp lệ!")
    private String email;

    @NotBlank(message = "Mật khẩu mới không được để trống!")
    @Size(min = 7, message = "Mật khẩu mới phải có độ dài lớn hơn 6 ký tự (từ 7 ký tự trở lên)!")
    private String newPassword;

    public PasswordResetInitRequestDto() {
    }

    public PasswordResetInitRequestDto(String email, String newPassword) {
        this.email = email;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

