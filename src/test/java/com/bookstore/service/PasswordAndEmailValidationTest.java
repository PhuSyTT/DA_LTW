package com.bookstore.service;

import com.bookstore.dto.ApiResponse;
import com.bookstore.dto.PasswordStrengthDto;
import com.bookstore.dto.RegisterInitRequestDto;
import com.bookstore.dto.UserDto;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.OtpVerificationRepository;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.security.JwtTokenProvider;
import com.bookstore.service.impl.AuthServiceImpl;
import com.bookstore.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordAndEmailValidationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private BranchRepository branchRepository;

    private AuthServiceImpl authService;
    private UserServiceImpl userService;

    private User existingUser;
    private Role sampleRole;

    @BeforeEach
    void setUp() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
        authService = new AuthServiceImpl(userRepository, roleRepository, otpVerificationRepository, passwordEncoder, emailService, jwtTokenProvider);
        userService = new UserServiceImpl(userRepository, roleRepository, branchRepository, passwordEncoder);

        sampleRole = new Role(5L, "CUSTOMER", "Độc giả");
        existingUser = User.builder()
                .id(5L)
                .email("user.book4life@gmail.com")
                .password("$2a$10$encodedPassword")
                .fullName("Hoàng Độc Giả")
                .role(sampleRole)
                .isActive(true)
                .build();
    }

    // ==========================================
    // 1. KIỂM TRA ĐỘ BẢO MẬT MẬT KHẨU (PASSWORD STRENGTH)
    // ==========================================

    @Test
    @DisplayName("Mật khẩu rỗng hoặc null trả về VERY_WEAK với điểm 0")
    void testPasswordStrength_EmptyOrNull() {
        PasswordStrengthDto nullResult = PasswordStrengthDto.evaluate(null);
        assertEquals(0, nullResult.getScore());
        assertEquals("VERY_WEAK", nullResult.getLevel());
        assertFalse(nullResult.isAcceptable());

        PasswordStrengthDto emptyResult = PasswordStrengthDto.evaluate("");
        assertEquals(0, emptyResult.getScore());
        assertEquals("VERY_WEAK", emptyResult.getLevel());
        assertFalse(emptyResult.isAcceptable());
    }

    @Test
    @DisplayName("Mật khẩu dưới 6 ký tự được đánh giá là Rất yếu")
    void testPasswordStrength_Short() {
        PasswordStrengthDto result = PasswordStrengthDto.evaluate("123ab");
        assertEquals(0, result.getScore());
        assertEquals("VERY_WEAK", result.getLevel());
        assertFalse(result.isHasMinLength());
        assertFalse(result.isAcceptable());
    }

    @Test
    @DisplayName("Mật khẩu <= 6 ký tự là Rất yếu; mật khẩu > 6 ký tự nhưng thiếu ký tự đặc biệt là Yếu/Trung bình")
    void testPasswordStrength_Weak() {
        PasswordStrengthDto veryWeak = PasswordStrengthDto.evaluate("abc123"); // 6 ký tự
        assertEquals(0, veryWeak.getScore());
        assertEquals("VERY_WEAK", veryWeak.getLevel());

        PasswordStrengthDto weak = PasswordStrengthDto.evaluate("abc1234"); // 7 ký tự nhưng không có ký tự đặc biệt
        assertTrue(weak.getScore() >= 1 && weak.getScore() <= 2);
        assertFalse(weak.isAcceptable(), "Thiếu ký tự đặc biệt nên chưa đạt chuẩn");
    }

    @Test
    @DisplayName("Mật khẩu thỏa mãn > 6 ký tự, có chữ, có số và ký tự đặc biệt đạt chuẩn")
    void testPasswordStrength_MediumToStrong() {
        // Thiếu ký tự đặc biệt -> chưa acceptable
        PasswordStrengthDto noSpecial = PasswordStrengthDto.evaluate("Matkhau12");
        assertFalse(noSpecial.isAcceptable(), "Phải có ký tự đặc biệt mới đạt chuẩn");

        // Đủ > 6 ký tự, có chữ, số, ký tự đặc biệt
        PasswordStrengthDto strong = PasswordStrengthDto.evaluate("Pass@123");
        assertTrue(strong.getScore() >= 3);
        assertTrue(strong.isHasMinLength());
        assertTrue(strong.isHasDigit());
        assertTrue(strong.isHasSpecialChar());
        assertTrue(strong.isAcceptable());
    }

    // ==========================================
    // 2. KIỂM TRA TRÙNG EMAIL TRỰC TIẾP
    // ==========================================

    @Test
    @DisplayName("Kiểm tra email trùng - Email đã tồn tại trả về available=false")
    void testCheckEmailAvailability_AlreadyExists() {
        when(userRepository.existsByEmail("user.book4life@gmail.com")).thenReturn(true);

        ApiResponse<Map<String, Object>> response = authService.checkEmailAvailability("user.book4life@gmail.com");

        assertFalse(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(false, response.getData().get("available"));
        assertEquals(true, response.getData().get("validFormat"));
        assertTrue(response.getMessage().contains("đã được sử dụng"));
    }

    @Test
    @DisplayName("Kiểm tra email trùng - Email chưa tồn tại trả về available=true")
    void testCheckEmailAvailability_Available() {
        when(userRepository.existsByEmail("new.customer@gmail.com")).thenReturn(false);

        ApiResponse<Map<String, Object>> response = authService.checkEmailAvailability("new.customer@gmail.com");

        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        assertEquals(true, response.getData().get("available"));
        assertEquals(true, response.getData().get("validFormat"));
        assertTrue(response.getMessage().contains("Email khả dụng"));
    }

    @Test
    @DisplayName("Kiểm tra email không đúng định dạng trả về validFormat=false")
    void testCheckEmailAvailability_InvalidFormat() {
        ApiResponse<Map<String, Object>> response = authService.checkEmailAvailability("invalid-email-format");

        assertFalse(response.isSuccess());
        assertEquals(false, response.getData().get("available"));
        assertEquals(false, response.getData().get("validFormat"));
        assertTrue(response.getMessage().contains("Định dạng email chưa hợp lệ"));
    }

    // ==========================================
    // 3. RÀNG BUỘC KHI ĐĂNG KÝ VÀ QUẢN LÝ USER
    // ==========================================

    @Test
    @DisplayName("Đăng ký tài khoản bị từ chối nếu email đã tồn tại")
    void testInitiateRegistration_RejectDuplicateEmail() {
        when(userRepository.existsByEmail("user.book4life@gmail.com")).thenReturn(true);

        RegisterInitRequestDto dto = new RegisterInitRequestDto("user.book4life@gmail.com", "Password@123", "User Name", "0901234567");
        ApiResponse<String> response = authService.initiateRegistration(dto);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Email này đã được sử dụng"));
        verify(otpVerificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đăng ký tài khoản bị từ chối nếu mật khẩu <= 6 ký tự hoặc thiếu số/ký tự đặc biệt")
    void testInitiateRegistration_RejectWeakPassword() {
        when(userRepository.existsByEmail("unique@gmail.com")).thenReturn(false);

        // Mật khẩu <= 6 ký tự
        RegisterInitRequestDto shortPass = new RegisterInitRequestDto("unique@gmail.com", "123456", "User Name", "0901234567");
        ApiResponse<String> res1 = authService.initiateRegistration(shortPass);
        assertFalse(res1.isSuccess());
        assertTrue(res1.getMessage().contains("lớn hơn 6 ký tự"));

        // Mật khẩu > 6 ký tự nhưng thiếu ký tự đặc biệt
        RegisterInitRequestDto noSpecial = new RegisterInitRequestDto("unique@gmail.com", "Matkhau12", "User Name", "0901234567");
        ApiResponse<String> res2 = authService.initiateRegistration(noSpecial);
        assertFalse(res2.isSuccess());
        assertTrue(res2.getMessage().contains("ký tự đặc biệt"));
    }

    @Test
    @DisplayName("Đăng ký tài khoản thành công với mật khẩu chuẩn > 6 ký tự, trả về mã OTP")
    void testInitiateRegistration_SuccessWithOtp() {
        when(userRepository.existsByEmail("unique@gmail.com")).thenReturn(false);

        RegisterInitRequestDto validDto = new RegisterInitRequestDto("unique@gmail.com", "Pass@123", "User Name", "0901234567");
        ApiResponse<String> res = authService.initiateRegistration(validDto);

        assertTrue(res.isSuccess());
        assertNotNull(res.getData(), "Phải trả về mã OTP để hỗ trợ dự phòng không nhận được mail");
        assertEquals(6, res.getData().length(), "Mã OTP phải có 6 chữ số");
        verify(otpVerificationRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Admin tạo User bị từ chối nếu email bị trùng trong hệ thống")
    void testCreateUser_RejectDuplicateEmail() {
        when(userRepository.existsByEmail("user.book4life@gmail.com")).thenReturn(true);

        UserDto userDto = new UserDto();
        userDto.setEmail("user.book4life@gmail.com");
        userDto.setPassword("Password@123");
        userDto.setFullName("User Test");
        userDto.setRoleId(5L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(userDto);
        });

        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    @Test
    @DisplayName("Admin cập nhật User bị từ chối nếu đổi sang email đã tồn tại của người khác")
    void testUpdateUser_RejectDuplicateEmail() {
        when(userRepository.findById(5L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail("other@gmail.com")).thenReturn(true);

        UserDto userDto = new UserDto();
        userDto.setEmail("other@gmail.com");
        userDto.setFullName("Updated Name");
        userDto.setRoleId(5L);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.updateUser(5L, userDto);
        });

        assertTrue(ex.getMessage().contains("đã tồn tại"));
    }

    // ==========================================
    // 4. BẮT BUỘC XÁC THỰC OTP MỚI ĐƯỢC ĐỔI MẬT KHẨU HOẶC SỬA PROFILE
    // ==========================================

    @Test
    @DisplayName("Đặt lại mật khẩu: Chỉ đổi mật khẩu trong DB khi xác nhận đúng mã OTP")
    void testResetPassword_OnlyWithValidOtp() {
        when(userRepository.findByEmail("user.book4life@gmail.com")).thenReturn(Optional.of(existingUser));

        com.bookstore.dto.PasswordResetInitRequestDto initDto = new com.bookstore.dto.PasswordResetInitRequestDto(
                "user.book4life@gmail.com", "NewPass@123"
        );
        ApiResponse<String> initRes = authService.initiatePasswordReset(initDto);
        assertTrue(initRes.isSuccess());
        String generatedOtp = initRes.getData();
        assertNotNull(generatedOtp);

        // Giả lập lưu OTP vào DB
        com.bookstore.entity.OtpVerification otpEntity = new com.bookstore.entity.OtpVerification(
                "user.book4life@gmail.com", generatedOtp, "$2a$10$newEncodedPass", null, null, java.time.LocalDateTime.now().plusMinutes(5), "RESET_PASSWORD"
        );
        when(otpVerificationRepository.findTopByEmailAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
                "user.book4life@gmail.com", "RESET_PASSWORD"
        )).thenReturn(Optional.of(otpEntity));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Thử xác thực với OTP SAI -> Thất bại, mật khẩu DB KHÔNG đổi
        com.bookstore.dto.VerifyOtpRequestDto wrongOtpDto = new com.bookstore.dto.VerifyOtpRequestDto(
                "user.book4life@gmail.com", "999999"
        );
        ApiResponse<String> failRes = authService.verifyOtpAndResetPassword(wrongOtpDto);
        assertFalse(failRes.isSuccess());
        assertNotEquals("$2a$10$newEncodedPass", existingUser.getPassword());

        // 2. Xác thực với OTP ĐÚNG -> Thành công, mật khẩu DB được cập nhật mới
        com.bookstore.dto.VerifyOtpRequestDto correctOtpDto = new com.bookstore.dto.VerifyOtpRequestDto(
                "user.book4life@gmail.com", generatedOtp
        );
        ApiResponse<String> successRes = authService.verifyOtpAndResetPassword(correctOtpDto);
        assertTrue(successRes.isSuccess());
        assertEquals("$2a$10$newEncodedPass", existingUser.getPassword());
        assertTrue(Boolean.TRUE.equals(otpEntity.getIsUsed()));
        verify(userRepository, atLeastOnce()).save(existingUser);
    }

    @Test
    @DisplayName("Thay đổi thông tin cá nhân: Chỉ cập nhật Họ tên & SĐT khi xác nhận đúng mã OTP")
    void testUpdateProfile_OnlyWithValidOtp() {
        when(userRepository.findByEmail("user.book4life@gmail.com")).thenReturn(Optional.of(existingUser));

        com.bookstore.dto.ProfileUpdateInitRequestDto initDto = new com.bookstore.dto.ProfileUpdateInitRequestDto(
                "Hoàng Độc Giả Mới", "0988776655"
        );
        ApiResponse<String> initRes = authService.initiateProfileUpdate("user.book4life@gmail.com", initDto);
        assertTrue(initRes.isSuccess());
        String generatedOtp = initRes.getData();
        assertNotNull(generatedOtp);

        com.bookstore.entity.OtpVerification otpEntity = new com.bookstore.entity.OtpVerification(
                "user.book4life@gmail.com", generatedOtp, "", "Hoàng Độc Giả Mới", "0988776655", java.time.LocalDateTime.now().plusMinutes(5), "UPDATE_PROFILE"
        );
        when(otpVerificationRepository.findTopByEmailAndOtpTypeAndIsUsedFalseOrderByCreatedAtDesc(
                "user.book4life@gmail.com", "UPDATE_PROFILE"
        )).thenReturn(Optional.of(otpEntity));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        // 1. Nhập OTP SAI -> Thất bại, họ tên và số điện thoại trong DB KHÔNG thay đổi
        com.bookstore.dto.VerifyOtpRequestDto wrongOtpDto = new com.bookstore.dto.VerifyOtpRequestDto(
                "user.book4life@gmail.com", "000000"
        );
        ApiResponse<UserDto> failRes = authService.verifyOtpAndUpdateProfile("user.book4life@gmail.com", wrongOtpDto);
        assertFalse(failRes.isSuccess());
        assertEquals("Hoàng Độc Giả", existingUser.getFullName());
        assertNull(existingUser.getPhone());

        // 2. Nhập OTP ĐÚNG -> Thành công, họ tên và số điện thoại được lưu vào DB
        com.bookstore.dto.VerifyOtpRequestDto correctOtpDto = new com.bookstore.dto.VerifyOtpRequestDto(
                "user.book4life@gmail.com", generatedOtp
        );
        ApiResponse<UserDto> successRes = authService.verifyOtpAndUpdateProfile("user.book4life@gmail.com", correctOtpDto);
        assertTrue(successRes.isSuccess());
        assertEquals("Hoàng Độc Giả Mới", existingUser.getFullName());
        assertEquals("0988776655", existingUser.getPhone());
        assertTrue(Boolean.TRUE.equals(otpEntity.getIsUsed()));
        verify(userRepository, atLeastOnce()).save(existingUser);
    }
}
