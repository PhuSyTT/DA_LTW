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
                email, otpCode, passwordHash, fullName, request.getPhone(), expiresAt, "REGISTER"
        );
        otpVerificationRepository.save(otpRecord);

        // 7. Gửi email qua SMTP Gmail thực tế đến hòm thư người dùng
        try {
            emailService.sendRegistrationOtp(email, otpCode);
        } catch (Exception ex) {
            log.error("Không thể gửi email OTP qua SMTP tới {}: {}", email, ex.getMessage(), ex);
            String detailMsg = (ex.getMessage() != null && ex.getMessage().contains("timed out"))
                    ? "Kết nối tới máy chủ Google SMTP bị gián đoạn (timeout). Vui lòng thử lại sau vài giây!"
                    : "Không thể gửi mã xác nhận đến email [" + email + "]. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau!";
            return ApiResponse.error(detailMsg);
        }

        log.info("=================================================================");
        log.info(">>> MÃ XÁC THỰC OTP ĐÃ GỬI THỰC TẾ TỚI EMAIL [{}]: [{}] <<<", email, otpCode);
        log.info("=================================================================");

        return ApiResponse.success(
                null,
                "Mã xác thực OTP gồm 6 chữ số đã được gửi trực tiếp đến hộp thư " + email + ". Vui lòng kiểm tra email (hoặc thư mục Spam) để lấy mã xác nhận!"
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
                .findTopByEmailAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(email, "REGISTER");

        if (otpOpt.isEmpty()) {
            otpOpt = otpVerificationRepository
                    .findTopByEmailAndIsUsedFalseOrderByCreatedAtDesc(email);
        }

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> initiatePasswordReset(PasswordResetInitRequestDto request) {
        String email = request.getEmail();
        String newPassword = request.getNewPassword();

        // 1. Kiểm tra tài khoản có tồn tại không
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy tài khoản liên kết với địa chỉ email " + email + "!");
        }

        // 2. Kiểm tra độ bảo mật mật khẩu mới (> 6 ký tự, có chữ cái, số, ký tự đặc biệt)
        PasswordStrengthDto strength = PasswordStrengthDto.evaluate(newPassword);
        if (!strength.isAcceptable()) {
            return ApiResponse.error("Mật khẩu mới không đạt chuẩn bảo mật: Mật khẩu phải có độ dài lớn hơn 6 ký tự, bao gồm cả chữ cái, chữ số và ký tự đặc biệt!");
        }

        // 3. Cooldown 60s
        Optional<OtpVerification> lastOtpOpt = otpVerificationRepository
                .findTopByEmailAndOtpTypeOrderByCreatedAtDesc(email, "RESET_PASSWORD");
        if (lastOtpOpt.isPresent()) {
            OtpVerification lastOtp = lastOtpOpt.get();
            if (lastOtp.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
                return ApiResponse.error("Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP mới!");
            }
            if (!Boolean.TRUE.equals(lastOtp.getIsUsed())) {
                lastOtp.setIsUsed(true);
                otpVerificationRepository.save(lastOtp);
            }
        }

        // 4. Rate limiting: max 5 lần / 15 phút
        long recentRequests = otpVerificationRepository.countByEmailAndOtpTypeAndCreatedAtAfter(
                email, "RESET_PASSWORD", LocalDateTime.now().minusMinutes(15));
        if (recentRequests >= 5) {
            return ApiResponse.error("Bạn đã yêu cầu gửi mã OTP quá nhiều lần. Vui lòng thử lại sau 15 phút!");
        }

        // 5. Sinh mã OTP 6 số
        String otpCode = String.format("%06d", 100000 + secureRandom.nextInt(900000));
        String passwordHash = passwordEncoder.encode(newPassword);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpVerification otpRecord = new OtpVerification(
                email, otpCode, passwordHash, null, null, expiresAt, "RESET_PASSWORD"
        );
        otpVerificationRepository.save(otpRecord);

        // 6. Gửi email OTP thực tế
        try {
            emailService.sendOtp(email, otpCode, "đặt lại mật khẩu");
        } catch (Exception ex) {
            log.error("Không thể gửi email OTP đặt lại mật khẩu tới {}: {}", email, ex.getMessage());
            return ApiResponse.error("Không thể gửi mã xác nhận đến email [" + email + "]. Vui lòng kiểm tra lại địa chỉ email hoặc thử lại sau!");
        }

        log.info("=================================================================");
        log.info(">>> MÃ XÁC THỰC OTP ĐỔI MẬT KHẨU ĐÃ GỬI THỰC TẾ TỚI EMAIL [{}]: [{}] <<<", email, otpCode);
        log.info("=================================================================");

        return ApiResponse.success(
                null,
                "Mã xác thực OTP đặt lại mật khẩu đã được gửi đến email " + email + ". Vui lòng kiểm tra email của bạn để lấy mã!"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> verifyOtpAndResetPassword(VerifyOtpRequestDto request) {
        String email = request.getEmail();
        String inputOtp = request.getOtp();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Tài khoản không tồn tại!");
        }

        Optional<OtpVerification> otpOpt = otpVerificationRepository
                .findTopByEmailAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(email, "RESET_PASSWORD");

        if (otpOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy yêu cầu đặt lại mật khẩu cho email này hoặc mã đã hết hạn. Vui lòng thử lại!");
        }

        OtpVerification otpRecord = otpOpt.get();

        if (otpRecord.isExpired()) {
            otpRecord.setIsUsed(true);
            otpVerificationRepository.save(otpRecord);
            return ApiResponse.error("Mã OTP đã hết hạn (quá 5 phút). Vui lòng yêu cầu mã xác nhận mới!");
        }

        if (otpRecord.getAttempts() >= 5) {
            otpRecord.setIsUsed(true);
            otpVerificationRepository.save(otpRecord);
            return ApiResponse.error("Bạn đã nhập sai mã OTP quá 5 lần. Phiên xác thực bị hủy vì lý do an toàn!");
        }

        if (!otpRecord.getOtpCode().equals(inputOtp)) {
            otpRecord.setAttempts(otpRecord.getAttempts() + 1);
            otpVerificationRepository.save(otpRecord);
            int remaining = 5 - otpRecord.getAttempts();
            return ApiResponse.error("Mã xác nhận OTP không chính xác! Bạn còn " + remaining + " lần thử.");
        }

        // OTP hợp lệ -> Cập nhật mật khẩu mới
        otpRecord.setIsUsed(true);
        otpVerificationRepository.save(otpRecord);

        User user = userOpt.get();
        user.setPassword(otpRecord.getPasswordHash());
        userRepository.save(user);

        log.info("Đã cập nhật mật khẩu mới thành công cho tài khoản ID={}, Email={}", user.getId(), user.getEmail());
        return ApiResponse.success("Đổi mật khẩu thành công! Bạn có thể đăng nhập ngay với mật khẩu mới.");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<String> initiateProfileUpdate(String email, ProfileUpdateInitRequestDto request) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Tài khoản không tồn tại!");
        }

        if (request.getFullName() == null || request.getFullName().trim().isBlank()) {
            return ApiResponse.error("Họ và tên không được để trống!");
        }

        // Cooldown 60s
        Optional<OtpVerification> lastOtpOpt = otpVerificationRepository
                .findTopByEmailAndOtpTypeOrderByCreatedAtDesc(email, "UPDATE_PROFILE");
        if (lastOtpOpt.isPresent()) {
            OtpVerification lastOtp = lastOtpOpt.get();
            if (lastOtp.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(60))) {
                return ApiResponse.error("Vui lòng đợi 60 giây trước khi yêu cầu gửi lại mã OTP mới!");
            }
            if (!Boolean.TRUE.equals(lastOtp.getIsUsed())) {
                lastOtp.setIsUsed(true);
                otpVerificationRepository.save(lastOtp);
            }
        }

        // Rate limit: max 5 lần / 15 phút
        long recentRequests = otpVerificationRepository.countByEmailAndOtpTypeAndCreatedAtAfter(
                email, "UPDATE_PROFILE", LocalDateTime.now().minusMinutes(15));
        if (recentRequests >= 5) {
            return ApiResponse.error("Bạn đã yêu cầu gửi mã OTP quá nhiều lần. Vui lòng thử lại sau 15 phút!");
        }

        // Sinh mã OTP 6 số
        String otpCode = String.format("%06d", 100000 + secureRandom.nextInt(900000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        OtpVerification otpRecord = new OtpVerification(
                email, otpCode, "", request.getFullName().trim(),
                request.getPhone() != null ? request.getPhone().trim() : "",
                expiresAt, "UPDATE_PROFILE"
        );
        otpVerificationRepository.save(otpRecord);

        // Gửi email OTP thực tế
        try {
            emailService.sendOtp(email, otpCode, "thay đổi thông tin cá nhân");
        } catch (Exception ex) {
            log.error("Không thể gửi email OTP cập nhật thông tin tới {}: {}", email, ex.getMessage());
            return ApiResponse.error("Không thể gửi mã xác nhận đến email [" + email + "]. Vui lòng thử lại sau!");
        }

        log.info("=================================================================");
        log.info(">>> MÃ XÁC THỰC OTP ĐỔI THÔNG TIN CÁ NHÂN ĐÃ GỬI THỰC TẾ TỚI EMAIL [{}]: [{}] <<<", email, otpCode);
        log.info("=================================================================");

        return ApiResponse.success(
                null,
                "Mã xác thực OTP cập nhật thông tin cá nhân đã được gửi đến email " + email + ". Vui lòng kiểm tra email của bạn để lấy mã!"
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<UserDto> verifyOtpAndUpdateProfile(String email, VerifyOtpRequestDto request) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("Tài khoản không tồn tại!");
        }

        String inputOtp = request.getOtp();
        Optional<OtpVerification> otpOpt = otpVerificationRepository
                .findTopByEmailAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(email, "UPDATE_PROFILE");

        if (otpOpt.isEmpty()) {
            return ApiResponse.error("Không tìm thấy yêu cầu cập nhật thông tin hoặc mã OTP đã hết hạn. Vui lòng thử lại!");
        }

        OtpVerification otpRecord = otpOpt.get();

        if (otpRecord.isExpired()) {
            otpRecord.setIsUsed(true);
            otpVerificationRepository.save(otpRecord);
            return ApiResponse.error("Mã OTP đã hết hạn (quá 5 phút). Vui lòng yêu cầu mã xác nhận mới!");
        }

        if (otpRecord.getAttempts() >= 5) {
            otpRecord.setIsUsed(true);
            otpVerificationRepository.save(otpRecord);
            return ApiResponse.error("Bạn đã nhập sai mã OTP quá 5 lần. Phiên xác thực bị hủy vì lý do an toàn!");
        }

        if (!otpRecord.getOtpCode().equals(inputOtp)) {
            otpRecord.setAttempts(otpRecord.getAttempts() + 1);
            otpVerificationRepository.save(otpRecord);
            int remaining = 5 - otpRecord.getAttempts();
            return ApiResponse.error("Mã xác nhận OTP không chính xác! Bạn còn " + remaining + " lần thử.");
        }

        // OTP hợp lệ -> Cập nhật thông tin cá nhân
        otpRecord.setIsUsed(true);
        otpVerificationRepository.save(otpRecord);

        User user = userOpt.get();
        user.setFullName(otpRecord.getFullName());
        user.setPhone(otpRecord.getPhone());
        User savedUser = userRepository.save(user);

        log.info("Đã cập nhật thông tin cá nhân thành công cho ID={}, Email={}", savedUser.getId(), savedUser.getEmail());

        UserDto dto = new UserDto();
        dto.setId(savedUser.getId());
        dto.setEmail(savedUser.getEmail());
        dto.setFullName(savedUser.getFullName());
        dto.setPhone(savedUser.getPhone());
        if (savedUser.getRole() != null) {
            dto.setRoleId(savedUser.getRole().getId());
            dto.setRoleName(savedUser.getRole().getRoleName());
        }

        return ApiResponse.success(dto, "Cập nhật thông tin cá nhân thành công!");
    }
}

