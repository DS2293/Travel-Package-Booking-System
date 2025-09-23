package com.tpbs.userservice.service.impl;

import com.tpbs.userservice.dto.*;
import com.tpbs.userservice.model.User;
import com.tpbs.userservice.repository.UserRepository;
import com.tpbs.userservice.service.UserService;
import com.tpbs.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        try {
            log.debug("Attempting login for email: {}", loginRequest.getEmail());
            
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                log.warn("Invalid password attempt for email: {}", loginRequest.getEmail());
                throw new BadCredentialsException("Invalid email or password");
            }

            if (!user.isEnabled()) {
                log.warn("Login attempt for disabled/unapproved account: {}", loginRequest.getEmail());
                throw new BadCredentialsException("Account is not approved or disabled");
            }

            String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getUserId(), user.getName());
            UserDto userDto = convertToDto(user);

            log.info("Successful login for user: {} with role: {}", user.getEmail(), user.getRole());
            return new AuthResponse(token, userDto, "Login successful");
        } catch (Exception e) {
            log.error("Authentication failed for email: {}, error: {}", loginRequest.getEmail(), e.getMessage());
            throw new BadCredentialsException("Authentication failed: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        try {
            log.debug("Attempting registration for email: {} with role: {}", 
                     registerRequest.getEmail(), registerRequest.getRole());
            
            if (existsByEmail(registerRequest.getEmail())) {
                log.warn("Registration attempt with existing email: {}", registerRequest.getEmail());
                throw new RuntimeException("Email already exists");
            }

            User user = new User();
            user.setName(registerRequest.getName());
            user.setEmail(registerRequest.getEmail());
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(registerRequest.getRole());
            user.setContactNumber(registerRequest.getContactNumber());
            user.setRegistrationDate(LocalDateTime.now());
            
            // Auto-approve customers and admins, agents need approval
            if ("customer".equalsIgnoreCase(registerRequest.getRole()) || "admin".equalsIgnoreCase(registerRequest.getRole())) {
                user.setApproval("approved");
            } else {
                user.setApproval("pending");
            }

            user = userRepository.save(user);
            UserDto userDto = convertToDto(user);

            String token = null;
            String message = "Registration successful";
            
            // Generate token for approved users (customers and admins)
            if (user.isEnabled()) {
                token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getUserId(), user.getName());
                log.info("User registered and auto-approved: {} with role: {}", user.getEmail(), user.getRole());
            } else {
                message = "Registration successful. Waiting for admin approval.";
                log.info("User registered pending approval: {} with role: {}", user.getEmail(), user.getRole());
            }

            return new AuthResponse(token, userDto, message);
        } catch (Exception e) {
            log.error("Registration failed for email: {}, error: {}", registerRequest.getEmail(), e.getMessage());
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<UserDto> getUserById(Long userId) {
        return userRepository.findById(userId)
                .map(this::convertToDto);
    }

    @Override
    public Optional<UserDto> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (existsByEmail(userDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = convertToEntity(userDto);
        user.setPassword(passwordEncoder.encode("defaultPassword123")); // Default password
        user.setRegistrationDate(LocalDateTime.now());
        
        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setName(userDto.getName());
        existingUser.setContactNumber(userDto.getContactNumber());
        existingUser.setRole(userDto.getRole());
        existingUser.setApproval(userDto.getApproval());

        // Only update email if it's different and not already taken
        if (!existingUser.getEmail().equals(userDto.getEmail())) {
            if (existsByEmail(userDto.getEmail())) {
                throw new RuntimeException("Email already exists");
            }
            existingUser.setEmail(userDto.getEmail());
        }

        existingUser = userRepository.save(existingUser);
        return convertToDto(existingUser);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getUsersByRole(String role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserDto> getPendingApprovals() {
        return userRepository.findByApproval("pending").stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setApproval("approved");
        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Override
    public UserDto rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setApproval("rejected");
        user = userRepository.save(user);
        return convertToDto(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public long countUsersByRole(String role) {
        return userRepository.countByRole(role);
    }

    @Override
    public long countPendingApprovals() {
        return userRepository.countPendingApprovals();
    }

    @Override
    public UserDto convertToDto(User user) {
        return new UserDto(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getContactNumber(),
                user.getApproval(),
                user.getRegistrationDate()
        );
    }

    @Override
    public User convertToEntity(UserDto userDto) {
        User user = new User();
        user.setUserId(userDto.getUserId());
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setRole(userDto.getRole());
        user.setContactNumber(userDto.getContactNumber());
        user.setApproval(userDto.getApproval());
        user.setRegistrationDate(userDto.getRegistrationDate());
        return user;
    }
} 