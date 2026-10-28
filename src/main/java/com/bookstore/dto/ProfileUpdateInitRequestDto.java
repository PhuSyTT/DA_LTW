package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProfileUpdateInitRequestDto {

    @NotBlank(message = "Họ và tên không được để trống!")
    @Size(min = 2, max = 100, message = "Họ và tên phải từ 2 đến 100 ký tự!")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự!")
    private String phone;

    public ProfileUpdateInitRequestDto() {
    }

    public ProfileUpdateInitRequestDto(String fullName, String phone) {
        this.fullName = fullName;
        this.phone = phone;
    }

    public String getFullName() {
        return fullName != null ? fullName.trim() : null;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone != null ? phone.trim() : null;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
