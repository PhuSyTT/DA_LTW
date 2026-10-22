package com.bookstore.service;

import com.bookstore.dto.*;

public interface AuthService {

    // Bước 1: Khởi tạo đăng ký, mã hóa mật khẩu, lưu tạm vào DB và gửi OTP qua Gmail
    ApiResponse<String> initiateRegistration(RegisterInitRequestDto request);

    // Bước 2: Xác thực mã OTP, tạo User chính thức, sinh JWT Token
    ApiResponse<AuthResponseDto> verifyOtpAndRegister(VerifyOtpRequestDto request);

    // Bước 3: Gửi lại mã OTP (kiểm tra Cooldown 60s)
    ApiResponse<String> resendOtp(ResendOtpRequestDto request);
}

