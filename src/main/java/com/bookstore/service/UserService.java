package com.bookstore.service;

import com.bookstore.dto.RoleDto;
import com.bookstore.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();
    List<RoleDto> getAllRoles();
    UserDto getUserById(Long id);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(Long id, UserDto userDto);
    void toggleUserStatus(Long id);
    void deleteUser(Long id);
}
