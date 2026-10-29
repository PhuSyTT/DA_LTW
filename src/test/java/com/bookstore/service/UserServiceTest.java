package com.bookstore.service;

import com.bookstore.entity.User;
import com.bookstore.repository.MasterOrderRepository;
import com.bookstore.repository.ReviewRepository;
import com.bookstore.repository.TradeInRequestRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.repository.WishlistRepository;
import com.bookstore.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MasterOrderRepository masterOrderRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private TradeInRequestRepository tradeInRequestRepository;

    @Mock
    private WishlistRepository wishlistRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService.setMasterOrderRepository(masterOrderRepository);
        userService.setReviewRepository(reviewRepository);
        userService.setTradeInRequestRepository(tradeInRequestRepository);
        userService.setWishlistRepository(wishlistRepository);
    }

    @Test
    @DisplayName("Không được phép xóa tài khoản Super Admin (ID=1)")
    void testDeleteSuperAdminThrowsException() {
        User superAdmin = new User();
        superAdmin.setId(1L);
        superAdmin.setEmail("admin@book4life.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(superAdmin));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            userService.deleteUser(1L);
        });

        assertTrue(ex.getMessage().contains("Super Admin"));
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Tài khoản đã có đơn hàng -> Tự động chuyển sang KHÓA VÔ HIỆU HÓA thay vì xóa cứng")
    void testDeleteUserWithOrdersSafelyDeactivates() {
        User user = new User();
        user.setId(5L);
        user.setEmail("customer@gmail.com");
        user.setIsActive(true);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(masterOrderRepository.existsByUserId(5L)).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            userService.deleteUser(5L);
        });

        assertTrue(ex.getMessage().contains("KHÓA VÔ HIỆU HÓA"));
        assertFalse(user.getIsActive());
        verify(userRepository).save(user);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Tài khoản mới không có dữ liệu ràng buộc -> Xóa cứng thành công")
    void testDeleteNewUserSuccessfully() {
        User user = new User();
        user.setId(99L);
        user.setEmail("newuser@gmail.com");
        user.setIsActive(true);

        when(userRepository.findById(99L)).thenReturn(Optional.of(user));
        when(masterOrderRepository.existsByUserId(99L)).thenReturn(false);
        when(reviewRepository.existsByUserId(99L)).thenReturn(false);
        when(tradeInRequestRepository.existsByUserId(99L)).thenReturn(false);

        assertDoesNotThrow(() -> {
            userService.deleteUser(99L);
        });

        verify(wishlistRepository).deleteByUserId(99L);
        verify(userRepository).delete(user);
        verify(userRepository).flush();
    }

    @Test
    @DisplayName("deactivateUser luôn đặt isActive = false")
    void testDeactivateUser() {
        User user = new User();
        user.setId(10L);
        user.setIsActive(true);

        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        userService.deactivateUser(10L);

        assertFalse(user.getIsActive());
        verify(userRepository).save(user);
    }
}

