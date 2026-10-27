package com.bookstore.service.impl;

import com.bookstore.dto.*;
import com.bookstore.entity.OtpVerification;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.OtpVerificationRepository;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtTokenProvider;
import com.bookstore.service.AuthService;
import com.bookstore.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           OtpVerificationRepository otpVerificationRepository,
                           PasswordEncoder passwordEncoder,
                           EmailService emailService,
                           JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.otpVerificationRepository = otpVerificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> initiateRegistration(RegisterInitRequestDto request) {
        String email = request.getEmail();

        // 1. Kiểm tra Email đã tồn tại trong hệ thống chưa
        if (email == null || email.isBlank()) {
            return ApiResponse.error("Vui lòng cung cấp địa chỉ email hợp lệ!");
        }
        if (userRepository.existsByEmail(email)) {
            return ApiResponse.error("Email này đã được sử dụng để đăng ký tài khoản! Vui lòng chọn email khác hoặc đăng nhập.");
        }

        // 1.1 Kiểm tra độ mật của mật khẩu người dùng theo yêu cầu (> 6 ký tự, có chữ, có số, có ký tự đặc biệt)
        String rawPassword = request.getPassword();
        if (rawPassword == null || rawPassword.length() <= 6) {
            return ApiResponse.error("Mật khẩu phải có độ dài lớn hơn 6 ký tự (từ 7 ký tự trở lên)!");
        }
        boolean hasLetter = rawPassword.matches(".*[a-zA-Z].*");
        boolean hasDigit = rawPassword.matches(".*[0-9].*");
        boolean hasSpecial = rawPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
        if (!hasLetter || !hasDigit || !hasSpecial) {
            return ApiResponse.error("Mật khẩu bắt buộc phải bao gồm cả chữ cái, chữ số và ký tự đặc biệt (!@#$...)!");
        }

        // 2. Chống Spam: Rate Limiting (Tối đa 5 lần yêu cầu OTP trong 15 phút)
        long recentRequests = otpVerificationRepository.countByEmailAndCreatedAtAfter(
                email, LocalDateTime.now().minusMinutes(15));
        if (recentRequests >= 5) {
            return ApiResponse.error("Bạn đã yêu cầu gửi mã OTP quá nhiều lần. Vui lòng thử lại sau 15 phút!");
        }

        // 3. Chống Spam: Cooldown (Chỉ được yêu cầu gửi lại sau 60 giây)
        Optional<OtpVerification> lastOtpOpt = otpVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (lastOtpOpt.isPresent()) {
            OtpVerification lastOtp = lastOtpOpt.get();
            if (lastOtp.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
                return ApiResponse.error("Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP mới!");
            }
            // Vô hiệu hóa mã cũ chưa dùng
            if (!Boolean.TRUE.equals(lastOtp.getIsUsed())) {
                lastOtp.setIsUsed(true);
                otpVerificationRepository.save(lastOtp);
            }
        }

        // 4. Sinh mã OTP 6 chữ số ngẫu nhiên an toàn (SecureRandom)
        String otpCode = String.format("%06d", 100000 + secureRandom.nextInt(900000));

        // 5. Mã hóa mật khẩu an toàn bằng BCrypt
        String passwordHash = passwordEncoder.encode(request.getPassword());

        String fullName = request.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = email.split("@")[0];
        }

        // 6. Lưu vào Database (Phương án B) với thời hạn 5 phút
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);
        OtpVerification otpRecord = new OtpVerification(
                email, otpCode, passwordHash, fullName, request.getPhone(), expiresAt
        );
        otpVerificationRepository.save(otpRecord);

        // 7. Gửi email qua SMTP Gmail
        try {
            emailService.sendRegistrationOtp(email, otpCode);
        } catch (Exception ex) {
            log.warn("Không thể gửi email OTP qua SMTP tới {}: {}", email, ex.getMessage());
        }

        log.info("=================================================================");
        log.info(">>> MÃ XÁC THỰC OTP ĐĂNG KÝ CHO [{}]: [{}] <<<", email, otpCode);
        log.info("=================================================================");

        return ApiResponse.success(
                otpCode,
                "Mã OTP đã được gửi đến email " + email + ". (Mã xác thực nhanh: " + otpCode + ")"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<AuthResponseDto> verifyOtpAndRegister(VerifyOtpRequestDto request) {
        String email = request.getEmail();
        String inputOtp = request.getOtp();

        // 1. Kiểm tra lại Email trùng lặp
        if (userRepository.existsByEmail(email)) {
            return ApiResponse.error("Email này đã được đăng ký tài khoản trước đó!");
        }

        // 2. Tìm bản ghi OTP mới nhất chưa sử dụng
        Optional<OtpVerification> otpOpt = otpVerificationRepository
                .findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(email);

        if (otpOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy yêu cầu đăng ký cho email này hoặc mã đã hết hạn. Vui lòng đăng ký lại!");
        }

        OtpVerification otpRecord = otpOpt.get();

        // 3. Kiểm tra thời hạn 5 phút
        if (otpRecord.isExpired()) {
            otpRecord.setIsUsed(true);
            otpVerificationRepository.save(otpRecord);
            return ApiResponse.error("Mã OTP đã hết hạn (quá 5 phút). Vui lòng yêu cầu mã xác nhận mới!");
        }

        // 4. Chống Brute-force: Khóa nếu nhập sai quá 5 lần
        if (otpRecord.getAttempts() >= 5) {
            otpRecord.setIsUsed(true);
            otpVerificationRepository.save(otpRecord);
            return ApiResponse.error("Bạn đã nhập sai mã OTP quá 5 lần. Phiên xác thực bị hủy vì lý do an toàn!");
        }

        // 5. So khớp mã OTP
        if (!otpRecord.getOtpCode().equals(inputOtp)) {
            otpRecord.setAttempts(otpRecord.getAttempts() + 1);
            otpVerificationRepository.save(otpRecord);
            int remaining = 5 - otpRecord.getAttempts();
            return ApiResponse.error("Mã xác nhận OTP không chính xác! Bạn còn " + remaining + " lần thử.");
        }

        // 6. OTP HỢP LỆ -> Đánh dấu đã sử dụng
        otpRecord.setIsUsed(true);
        otpVerificationRepository.save(otpRecord);

        // 7. Lấy vai trò mặc định cho Độc giả
        Role customerRole = roleRepository.findByRoleName("ROLE_CUSTOMER")
                .orElseGet(() -> {
                    Role r = new Role(null, "ROLE_CUSTOMER", "Khách hàng");
                    return roleRepository.save(r);
                });

        // 8. Tạo và lưu Người dùng chính thức vào bảng users
        User newUser = new User();
        newUser.setEmail(otpRecord.getEmail());
        newUser.setPassword(otpRecord.getPasswordHash());
        newUser.setFullName(otpRecord.getFullName());
        newUser.setPhone(otpRecord.getPhone());
        newUser.setRole(customerRole);
        newUser.setIsActive(true);
        newUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);
        log.info("Đã tạo mới thành công tài khoản người dùng: ID={}, Email={}", savedUser.getId(), savedUser.getEmail());

        // 9. Sinh mã JWT Token để tự động đăng nhập luôn
        String jwtToken = jwtTokenProvider.generateToken(savedUser);

        AuthResponseDto responseDto = new AuthResponseDto(
                jwtToken,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                customerRole.getRoleName(),
                "Xác thực mã OTP và đăng ký tài khoản thành công!"
        );

        return ApiResponse.success(responseDto, "Đăng ký tài khoản thành công!");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> resendOtp(ResendOtpRequestDto request) {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) {
            return ApiResponse.error("Email này đã có tài khoản, không thể yêu cầu mã đăng ký mới!");
        }

        // 1. Kiểm tra Cooldown 60 giây
        Optional<OtpVerification> lastOtpOpt = otpVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email);
        if (lastOtpOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy phiên đăng ký cho email này. Vui lòng nhập email và mật khẩu để bắt đầu!");
        }

        OtpVerification lastOtp = lastOtpOpt.get();
        if (lastOtp.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
            return ApiResponse.error("Vui lòng đợi 60 giây trước khi bấm gửi lại mã OTP!");
        }

        // 2. Chống Spam: Max 5 lần trong 15 phút
        long recentRequests = otpVerificationRepository.countByEmailAndCreatedAtAfter(
                email, LocalDateTime.now().minusMinutes(15));
        if (recentRequests >= 5) {
            return ApiResponse.error("Bạn đã yêu cầu gửi mã quá nhiều lần. Vui lòng thử lại sau 15 phút!");
        }

        // 3. Vô hiệu hóa bản ghi cũ
        lastOtp.setIsUsed(true);
        otpVerificationRepository.save(lastOtp);

        // 4. Sinh mã OTP mới và gia hạn thêm 5 phút
        String newOtpCode = String.format("%06d", 100000 + secureRandom.nextInt(900000));
        LocalDateTime newExpiresAt = LocalDateTime.now().plusMinutes(5);

        OtpVerification newOtpRecord = new OtpVerification(
                email,
                newOtpCode,
                lastOtp.getPasswordHash(),
                lastOtp.getFullName(),
                lastOtp.getPhone(),
                newExpiresAt
        );
        otpVerificationRepository.save(newOtpRecord);

        // 5. Gửi email
        try {
            emailService.sendRegistrationOtp(email, newOtpCode);
        } catch (Exception ex) {
            log.warn("Không thể gửi email OTP qua SMTP tới {}: {}", email, ex.getMessage());
        }

        log.info("=================================================================");
        log.info(">>> MÃ XÁC THỰC OTP GỬI LẠI CHO [{}]: [{}] <<<", email, newOtpCode);
        log.info("=================================================================");

        return ApiResponse.success(
                newOtpCode,
                "Mã xác nhận mới đã được gửi đến email " + email + ". (Mã xác thực nhanh: " + newOtpCode + ")"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<java.util.Map<String, Object>> checkEmailAvailability(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ApiResponse.error("Email không được để trống!");
        }
        String cleanEmail = email.trim().toLowerCase();
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        if (!cleanEmail.matches(emailRegex)) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("email", cleanEmail);
            data.put("available", false);
            data.put("validFormat", false);
            return new ApiResponse<>(false, "Định dạng email chưa hợp lệ!", data);
        }

        boolean exists = userRepository.existsByEmail(cleanEmail);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("email", cleanEmail);
        data.put("available", !exists);
        data.put("validFormat", true);

        if (exists) {
            return new ApiResponse<>(false, "Email này đã được sử dụng trong hệ thống!", data);
        } else {
            return new ApiResponse<>(true, "Email khả dụng, bạn có thể sử dụng email này!", data);
        }
    }

    @Override
    public ApiResponse<PasswordStrengthDto> checkPasswordStrength(String password) {
        PasswordStrengthDto strength = PasswordStrengthDto.evaluate(password);
        return ApiResponse.success(strength, "Đánh giá độ bảo mật mật khẩu thành công");
    }
}

