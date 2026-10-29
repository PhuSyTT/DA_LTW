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
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + id));

        if (id == 1L) {
            throw new IllegalArgumentException("Không được phép xóa tài khoản Quản trị viên tối cao (Super Admin)!");
        }

        // Kiểm tra nếu tài khoản có dữ liệu ràng buộc quan trọng (đơn hàng, đánh giá, thu cũ đổi mới)
        boolean hasOrders = masterOrderRepository != null && masterOrderRepository.existsByUserId(id);
        boolean hasReviews = reviewRepository != null && reviewRepository.existsByUserId(id);
        boolean hasTradeIns = tradeInRequestRepository != null && tradeInRequestRepository.existsByUserId(id);

        if (hasOrders || hasReviews || hasTradeIns) {
            user.setIsActive(false);
            userRepository.save(user);
            StringBuilder sb = new StringBuilder();
            if (hasOrders) sb.append("đơn hàng");
            if (hasReviews) sb.append(sb.length() > 0 ? ", " : "").append("đánh giá sản phẩm");
            if (hasTradeIns) sb.append(sb.length() > 0 ? ", " : "").append("yêu cầu thu cũ");
            throw new IllegalStateException("Tài khoản [" + user.getEmail() + "] đã phát sinh dữ liệu (" + sb + ") trong hệ thống. Để bảo toàn chứng từ kế toán và toàn vẹn dữ liệu, hệ thống đã tự động chuyển sang trạng thái [KHÓA VÔ HIỆU HÓA] thay vì xóa hoàn toàn!");
        }

        // Xóa dữ liệu phụ trợ tạm thời nếu có (danh sách yêu thích)
        if (wishlistRepository != null) {
            try {
                wishlistRepository.deleteByUserId(id);
            } catch (Exception ignored) {}
        }

        userRepository.delete(user);
        userRepository.flush();
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
