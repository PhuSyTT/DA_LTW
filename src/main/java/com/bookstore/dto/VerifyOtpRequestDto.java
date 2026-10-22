package com.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyOtpRequestDto {

    @NotBlank(message = "Email không được để trống!")
    @Email(message = "Email không đúng định dạng!")
    private String email;

    @NotBlank(message = "Mã OTP không được để trống!")
    @Size(min = 6, max = 6, message = "Mã OTP phải có đúng 6 chữ số!")
    private String otp;

    public VerifyOtpRequestDto() {
    }

    public VerifyOtpRequestDto(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    public String getEmail() {
        return email != null ? email.trim().toLowerCase() : null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp != null ? otp.trim() : null;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}

