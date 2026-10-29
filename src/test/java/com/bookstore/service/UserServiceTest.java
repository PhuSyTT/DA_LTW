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
    @DisplayName("Xóa tài khoản thành công và cascade toàn bộ dữ liệu phụ thuộc")
    void testDeleteUserCascadedDataAndUserSuccessfully() {
        User user = new User();
        user.setId(5L);
        user.setEmail("customer@gmail.com");
        user.setIsActive(true);

        when(userRepository.findById(5L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> {
            userService.deleteUser(5L);
        });

        verify(wishlistRepository).deleteByUserId(5L);
        verify(userRepository).delete(user);
        verify(userRepository).flush();
    }

    @Test
    @DisplayName("Xóa tài khoản có DataSource -> Thực thi toàn bộ lệnh cascade SQL trên database")
    void testDeleteUserWithDataSourceExecutesCascadeSql() {
        java.util.concurrent.atomic.AtomicBoolean committed = new java.util.concurrent.atomic.AtomicBoolean(false);

        java.sql.Connection fakeConn = (java.sql.Connection) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{java.sql.Connection.class},
                (proxy, method, args) -> {
                    if ("commit".equals(method.getName())) {
                        committed.set(true);
                    }
                    if ("prepareStatement".equals(method.getName())) {
                        return java.lang.reflect.Proxy.newProxyInstance(
                                getClass().getClassLoader(),
                                new Class<?>[]{java.sql.PreparedStatement.class},
                                (psProxy, psMethod, psArgs) -> {
                                    if ("executeUpdate".equals(psMethod.getName())) return 1;
                                    return null;
                                }
                        );
                    }
                    if ("getAutoCommit".equals(method.getName())) return true;
                    return null;
                }
        );

        javax.sql.DataSource fakeDs = (javax.sql.DataSource) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class<?>[]{javax.sql.DataSource.class},
                (proxy, method, args) -> {
                    if ("getConnection".equals(method.getName())) return fakeConn;
                    return null;
                }
        );

        userService.setDataSource(fakeDs);

        User user = new User();
        user.setId(7L);
        user.setEmail("user7@gmail.com");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> {
            userService.deleteUser(7L);
        });

        assertTrue(committed.get());
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

