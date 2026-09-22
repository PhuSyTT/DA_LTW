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
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           BranchRepository branchRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
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
                : "123456";

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
            user.setPassword(passwordEncoder.encode(userDto.getPassword().trim()));
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
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng ID: " + id);
        }
        userRepository.deleteById(id);
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
