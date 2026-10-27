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

    /**
     * Kiểm tra tính khả dụng và trùng lặp của Email theo thời gian thực
     * GET /api/auth/check-email?email=...
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> checkEmail(@RequestParam(value = "email", required = false) String email) {
        ApiResponse<java.util.Map<String, Object>> response = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Kiểm tra tính khả dụng của Email (hỗ trợ POST payload)
     * POST /api/auth/check-email
     */
    @PostMapping("/check-email")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> checkEmailPost(@RequestBody java.util.Map<String, String> body) {
        String email = body != null ? body.get("email") : null;
        ApiResponse<java.util.Map<String, Object>> response = authService.checkEmailAvailability(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh giá độ bảo mật và độ mạnh của mật khẩu tài khoản người dùng
     * POST /api/auth/check-password-strength
     */
    @PostMapping("/check-password-strength")
    public ResponseEntity<ApiResponse<PasswordStrengthDto>> checkPasswordStrength(@RequestBody(required = false) java.util.Map<String, String> body) {
        String password = body != null ? body.get("password") : "";
        ApiResponse<PasswordStrengthDto> response = authService.checkPasswordStrength(password);
        return ResponseEntity.ok(response);
    }

    /**
     * Đánh giá độ bảo mật mật khẩu (hỗ trợ GET query param)
     * GET /api/auth/check-password-strength?password=...
     */
    @GetMapping("/check-password-strength")
    public ResponseEntity<ApiResponse<PasswordStrengthDto>> checkPasswordStrengthGet(@RequestParam(value = "password", required = false) String password) {
        ApiResponse<PasswordStrengthDto> response = authService.checkPasswordStrength(password != null ? password : "");
        return ResponseEntity.ok(response);
    }
}

