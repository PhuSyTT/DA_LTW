package com.bookstore.controller.api;

import com.bookstore.dto.*;
import com.bookstore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthService authService;

    public AuthApiController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * BƯỚC 1: Khởi tạo đăng ký, mã hóa mật khẩu, lưu OTP tạm vào DB (5 phút) và gửi mail
     * POST /api/auth/register-init
     */
    @PostMapping("/register-init")
    public ResponseEntity<ApiResponse<String>> registerInit(@Valid @RequestBody RegisterInitRequestDto request) {
        ApiResponse<String> response = authService.initiateRegistration(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * BƯỚC 2: Nhận mã OTP từ client, kiểm tra tính hợp lệ và hạn 5 phút -> Lưu User chính thức, trả về JWT
     * POST /api/auth/verify-otp
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponseDto>> verifyOtp(@Valid @RequestBody VerifyOtpRequestDto request) {
        ApiResponse<AuthResponseDto> response = authService.verifyOtpAndRegister(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * BƯỚC 3: Gửi lại mã OTP (kiểm tra Cooldown 60s và Rate limiting)
     * POST /api/auth/resend-otp
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@Valid @RequestBody ResendOtpRequestDto request) {
        ApiResponse<String> response = authService.resendOtp(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}

