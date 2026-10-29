package com.bookstore.service.impl;

import com.bookstore.dto.RoleDto;
import com.bookstore.dto.UserDto;
import com.bookstore.entity.Branch;
import com.bookstore.entity.Role;
import com.bookstore.entity.User;
import com.bookstore.repository.BranchRepository;
import com.bookstore.repository.RoleRepository;
import com.bookstore.repository.UserRepository;
import com.bookstore.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;
    private DataSource dataSource;
    private com.bookstore.repository.MasterOrderRepository masterOrderRepository;
    private com.bookstore.repository.ReviewRepository reviewRepository;
    private com.bookstore.repository.TradeInRequestRepository tradeInRequestRepository;
    private com.bookstore.repository.WishlistRepository wishlistRepository;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BranchRepository branchRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setMasterOrderRepository(com.bookstore.repository.MasterOrderRepository masterOrderRepository) {
        this.masterOrderRepository = masterOrderRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setReviewRepository(com.bookstore.repository.ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setTradeInRequestRepository(com.bookstore.repository.TradeInRequestRepository tradeInRequestRepository) {
        this.tradeInRequestRepository = tradeInRequestRepository;
    }

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    public void setWishlistRepository(com.bookstore.repository.WishlistRepository wishlistRepository) {
        this.wishlistRepository = wishlistRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .sorted(Comparator.comparing(User::getId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(r -> new RoleDto(r.getId(), r.getRoleName(), r.getDescription()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));
        return mapToDto(user);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDto.getEmail() + "' đã tồn tại trong hệ thống!");
        }

        Role role = roleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò ID: " + userDto.getRoleId()));

        Branch branch = null;
        if (userDto.getBranchId() != null) {
            branch = branchRepository.findById(userDto.getBranchId()).orElse(null);
        }

        String rawPassword = (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty())
                ? userDto.getPassword().trim()
                : "1234@abc";

        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            if (rawPassword.length() <= 6) {
                throw new IllegalArgumentException("Mật khẩu phải có độ dài lớn hơn 6 ký tự (từ 7 ký tự trở lên)!");
            }
            boolean hasLetter = rawPassword.matches(".*[a-zA-Z].*");
            boolean hasDigit = rawPassword.matches(".*[0-9].*");
            boolean hasSpecial = rawPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
            if (!hasLetter || !hasDigit || !hasSpecial) {
                throw new IllegalArgumentException("Mật khẩu bắt buộc phải bao gồm cả chữ cái, chữ số và ký tự đặc biệt (!@#$...)!");
            }
        }

        User user = User.builder()
                .email(userDto.getEmail().trim())
                .password(passwordEncoder.encode(rawPassword))
                .fullName(userDto.getFullName().trim())
                .phone(userDto.getPhone())
                .avatarUrl(userDto.getAvatarUrl())
                .role(role)
                .branch(branch)
                .isActive(userDto.getIsActive() != null ? userDto.getIsActive() : true)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));

        if (!user.getEmail().equalsIgnoreCase(userDto.getEmail().trim()) && userRepository.existsByEmail(userDto.getEmail())) {
            throw new IllegalArgumentException("Email '" + userDto.getEmail() + "' đã tồn tại!");
        }

        Role role = roleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò ID: " + userDto.getRoleId()));

        Branch branch = null;
        if (userDto.getBranchId() != null) {
            branch = branchRepository.findById(userDto.getBranchId()).orElse(null);
        }

        user.setEmail(userDto.getEmail().trim());
        user.setFullName(userDto.getFullName().trim());
        user.setPhone(userDto.getPhone());
        user.setRole(role);
        user.setBranch(branch);

        if (userDto.getPassword() != null && !userDto.getPassword().trim().isEmpty()) {
            String newPassword = userDto.getPassword().trim();
            if (newPassword.length() <= 6) {
                throw new IllegalArgumentException("Mật khẩu mới phải có độ dài lớn hơn 6 ký tự (từ 7 ký tự trở lên)!");
            }
            boolean hasLetter = newPassword.matches(".*[a-zA-Z].*");
            boolean hasDigit = newPassword.matches(".*[0-9].*");
            boolean hasSpecial = newPassword.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
            if (!hasLetter || !hasDigit || !hasSpecial) {
                throw new IllegalArgumentException("Mật khẩu mới bắt buộc phải bao gồm cả chữ cái, chữ số và ký tự đặc biệt (!@#$...)!");
            }
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        if (userDto.getIsActive() != null) {
            user.setIsActive(userDto.getIsActive());
        }

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Override
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));
        user.setIsActive(!Boolean.TRUE.equals(user.getIsActive()));
        userRepository.save(user);
    }

    @Override
    public void deactivateUser(Long id) {
        userRepository.findById(id).ifPresent(user -> {
            user.setIsActive(false);
            userRepository.save(user);
        });
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));

        if (id == 1L) {
            throw new IllegalArgumentException("Không được phép xóa tài khoản Quản trị viên tối cao (Super Admin)!");
        }

        // Xóa sạch toàn bộ dữ liệu ràng buộc liên quan (đơn hàng, đánh giá, yêu cầu thu cũ, wishlist, chat...)
        if (dataSource != null) {
            cascadeDeleteUserData(id);
        } else {
            if (wishlistRepository != null) {
                try { wishlistRepository.deleteByUserId(id); } catch (Exception ignored) {}
            }
        }

        userRepository.delete(user);
        userRepository.flush();
    }

    private void cascadeDeleteUserData(Long userId) {
        try (Connection conn = dataSource.getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // 1. Xóa danh sách yêu thích
                executeSql(conn, "DELETE FROM wishlists WHERE user_id = ?", userId);

                // 2. Xóa các đánh giá sách do người dùng viết
                executeSql(conn, "DELETE FROM reviews WHERE user_id = ?", userId);

                // 3. Xóa tin nhắn AI chat & tin nhắn chat nội bộ nếu có
                executeSql(conn, "DELETE FROM ai_chat_messages WHERE user_id = ?", userId);
                executeSql(conn, "DELETE FROM chat_messages WHERE sender_id = ? OR receiver_id = ?", userId, userId);

                // 4. Xóa ảnh và yêu cầu thu cũ đổi mới của người dùng
                executeSql(conn, "DELETE FROM trade_in_images WHERE trade_in_request_id IN (SELECT id FROM trade_in_requests WHERE user_id = ?)", userId);
                executeSql(conn, "DELETE FROM trade_in_requests WHERE user_id = ?", userId);
                executeSql(conn, "UPDATE trade_in_requests SET reviewed_by_staff_id = NULL WHERE reviewed_by_staff_id = ?", userId);

                // 5. Gỡ liên kết người yêu cầu chuyển kho (nếu tài khoản là nhân viên)
                executeSql(conn, "UPDATE stock_transfers SET requested_by_user_id = NULL WHERE requested_by_user_id = ?", userId);

                // 6. Gỡ người cập nhật lịch sử trạng thái & xóa lịch sử trạng thái của các đơn hàng thuộc về người dùng này
                executeSql(conn, "UPDATE order_status_history SET updated_by_user_id = NULL WHERE updated_by_user_id = ?", userId);
                executeSql(conn, "DELETE FROM order_status_history WHERE master_order_id IN (SELECT id FROM master_orders WHERE user_id = ?)", userId);

                // 7. Xóa lịch sử sử dụng mã coupon liên quan đến người dùng hoặc đơn hàng của người dùng
                executeSql(conn, "DELETE FROM coupon_usages WHERE user_id = ? OR master_order_id IN (SELECT id FROM master_orders WHERE user_id = ?)", userId, userId);

                // 8. Xóa chi tiết mặt hàng đơn hàng con (sub_order_items)
                executeSql(conn, "DELETE FROM sub_order_items WHERE sub_order_id IN (SELECT id FROM sub_orders WHERE master_order_id IN (SELECT id FROM master_orders WHERE user_id = ?))", userId);

                // 9. Xóa đơn hàng con (sub_orders)
                executeSql(conn, "DELETE FROM sub_orders WHERE master_order_id IN (SELECT id FROM master_orders WHERE user_id = ?)", userId);

                // 10. Xóa đơn hàng tổng (master_orders)
                executeSql(conn, "DELETE FROM master_orders WHERE user_id = ?", userId);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw new RuntimeException("Lỗi khi xóa dữ liệu liên kết của người dùng: " + e.getMessage(), e);
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Lỗi kết nối CSDL khi xóa dữ liệu người dùng: " + e.getMessage(), e);
        }
    }

    private void executeSql(Connection conn, String sql, Object... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            String msg = (ex.getMessage() != null) ? ex.getMessage().toLowerCase() : "";
            // Bỏ qua nếu bảng không tồn tại trong CSDL
            if (!msg.contains("doesn't exist") && !msg.contains("not found") && !msg.contains("invalid object name")) {
                throw new RuntimeException("Lỗi thực thi [" + sql + "]: " + ex.getMessage(), ex);
            }
        }
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setPhone(user.getPhone());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setIsActive(user.getIsActive());
        dto.setCreatedAt(user.getCreatedAt());

        if (user.getRole() != null) {
            dto.setRoleId(user.getRole().getId());
            dto.setRoleName(user.getRole().getRoleName());
            dto.setRoleDescription(user.getRole().getDescription());
        }

        if (user.getBranch() != null) {
            dto.setBranchId(user.getBranch().getId());
            dto.setBranchName(user.getBranch().getBranchName());
        }

        return dto;
    }
}
