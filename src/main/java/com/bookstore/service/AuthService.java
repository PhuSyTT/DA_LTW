package com.bookstore.service;

import com.bookstore.dto.*;

public interface AuthService {

    // Bước 1: Khởi tạo đăng ký, mã hóa mật khẩu, lưu tạm vào DB và gửi OTP qua Gmail
    ApiResponse<String> initiateRegistration(RegisterInitRequestDto request);

    // Bước 2: Xác thực mã OTP, tạo User chính thức, sinh JWT Token
    ApiResponse<AuthResponseDto> verifyOtpAndRegister(VerifyOtpRequestDto request);

    // Bước 3: Gửi lại mã OTP (kiểm tra Cooldown 60s)
    ApiResponse<String> resendOtp(ResendOtpRequestDto request);

    // Kiểm tra email trùng lặp khả dụng theo thời gian thực
    ApiResponse<java.util.Map<String, Object>> checkEmailAvailability(String email);

    // Đánh giá độ bảo mật và độ mạnh của mật khẩu người dùng
    ApiResponse<PasswordStrengthDto> checkPasswordStrength(String password);

    // Bước 1: Khởi tạo yêu cầu đổi/đặt lại mật khẩu, gửi OTP qua Gmail
    ApiResponse<String> initiatePasswordReset(PasswordResetInitRequestDto request);

    // Bước 2: Xác thực mã OTP và cập nhật mật khẩu mới
    ApiResponse<String> verifyOtpAndResetPassword(VerifyOtpRequestDto request);

    // Bước 1: Khởi tạo yêu cầu thay đổi thông tin cá nhân (Họ tên, SĐT), gửi OTP qua Gmail
    ApiResponse<String> initiateProfileUpdate(String email, ProfileUpdateInitRequestDto request);

    // Bước 2: Xác thực mã OTP và cập nhật thông tin cá nhân
    ApiResponse<UserDto> verifyOtpAndUpdateProfile(String email, VerifyOtpRequestDto request);
}

