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
    @DisplayName("Mật khẩu 6-7 ký tự chỉ toàn chữ hoặc số được đánh giá là Rất yếu; có cả chữ và số là Yếu")
    void testPasswordStrength_Weak() {
        PasswordStrengthDto veryWeak = PasswordStrengthDto.evaluate("abcdef");
        assertEquals("VERY_WEAK", veryWeak.getLevel());

        PasswordStrengthDto weak = PasswordStrengthDto.evaluate("abc123");
        assertEquals(1, weak.getScore());
        assertEquals("WEAK", weak.getLevel());
    }

    @Test
    @DisplayName("Mật khẩu từ 8 ký tự kết hợp chữ hoa, thường và số đạt mức Khá/Mạnh")
    void testPasswordStrength_MediumToStrong() {
        PasswordStrengthDto medium = PasswordStrengthDto.evaluate("Matkhau12");
        assertTrue(medium.getScore() >= 2);
        assertTrue(medium.isHasMinLength());
        assertTrue(medium.isHasDigit());
        assertTrue(medium.isAcceptable());

        PasswordStrengthDto strong = PasswordStrengthDto.evaluate("Matkhau12@#$");
        assertEquals(4, strong.getScore());
        assertEquals("VERY_STRONG", strong.getLevel());
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
    @DisplayName("Đăng ký tài khoản bị từ chối nếu mật khẩu quá yếu hoặc dưới 6 ký tự")
    void testInitiateRegistration_RejectWeakPassword() {
        when(userRepository.existsByEmail("unique@gmail.com")).thenReturn(false);

        RegisterInitRequestDto dto = new RegisterInitRequestDto("unique@gmail.com", "123", "User Name", "0901234567");
        ApiResponse<String> response = authService.initiateRegistration(dto);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("từ 6 ký tự trở lên"));
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
}
