package com.tpbs.userservice.service;

import com.tpbs.userservice.dto.*;
import com.tpbs.userservice.model.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {
    
    // Authentication methods
    AuthResponse login(LoginRequest loginRequest);
    AuthResponse register(RegisterRequest registerRequest);
    
    // User CRUD operations
    List<UserDto> getAllUsers();
    Optional<UserDto> getUserById(Long userId);
    Optional<UserDto> getUserByEmail(String email);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(Long userId, UserDto userDto);
    void deleteUser(Long userId);
    
    // Role-based operations
    List<UserDto> getUsersByRole(String role);
    List<UserDto> getPendingApprovals();
    UserDto approveUser(Long userId);
    UserDto rejectUser(Long userId);
    
    // Utility methods
    boolean existsByEmail(String email);
    long countUsersByRole(String role);
    long countPendingApprovals();
    
    // Convert between Entity and DTO
    UserDto convertToDto(User user);
    User convertToEntity(UserDto userDto);
} 