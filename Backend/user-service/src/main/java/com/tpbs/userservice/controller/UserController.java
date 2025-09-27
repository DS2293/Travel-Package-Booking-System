package com.tpbs.userservice.controller;

import com.tpbs.userservice.dto.UserDto;
import com.tpbs.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Helper method to check if user has admin role
    private boolean isAdmin(HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");
        log.debug("Checking admin access - User role from header: {}", userRole);
        return "admin".equalsIgnoreCase(userRole);
    }

    // Helper method to check if user has admin or agent role, or if it's an internal service call
    private boolean isAdminOrAgent(HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");
        String serviceCall = request.getHeader("X-Service-Call");
        
        log.debug("Checking access - User role: {}, Service call: {}", userRole, serviceCall);
        
        // Allow internal service-to-service calls
        if ("internal".equalsIgnoreCase(serviceCall)) {
            return true;
        }
        
        // Allow admin and agent users
        return "admin".equalsIgnoreCase(userRole) || "agent".equalsIgnoreCase(userRole);
    }

    // Helper method to get user info from headers
    private Map<String, String> getUserInfo(HttpServletRequest request) {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("role", request.getHeader("X-User-Role"));
        userInfo.put("email", request.getHeader("X-User-Email"));
        userInfo.put("userId", request.getHeader("X-User-Id"));
        return userInfo;
    }

    // Helper method to return 403 Forbidden
    private ResponseEntity<?> forbidden() {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Access denied. Admin role required.");
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        List<UserDto> users = userService.getAllUsers();
        log.info("Admin {} retrieved all users", request.getHeader("X-User-Email"));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUserById(@PathVariable Long id, HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");
        String serviceCall = request.getHeader("X-Service-Call");
        log.info("🔍 getUserById called - ID: {}, Role: {}, ServiceCall: {}", id, userRole, serviceCall);
        
        if (!isAdminOrAgent(request)) {
            log.warn("❌ Access denied for getUserById - ID: {}, Role: {}, ServiceCall: {}", id, userRole, serviceCall);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Access denied. Admin or Agent role required.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        Optional<UserDto> userOpt = userService.getUserById(id);
        String requesterEmail = request.getHeader("X-User-Email");
        String requesterRole = request.getHeader("X-User-Role");
        
        if (userOpt.isPresent()) {
            UserDto user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("contactNumber", user.getContactNumber());
            userData.put("role", user.getRole());
            
            response.put("success", true);
            response.put("data", userData);
            
            log.info("✅ Successfully retrieved user - ID: {}, Name: {}, Role: {}, ServiceCall: {}", 
                id, user.getName(), userRole, serviceCall);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        Optional<UserDto> user = userService.getUserByEmail(email);
        log.info("Admin {} retrieved user with email: {}", request.getHeader("X-User-Email"), email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    public ResponseEntity<?> getUsersByRole(@PathVariable String role, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        List<UserDto> users = userService.getUsersByRole(role);
        log.info("Admin {} retrieved users with role: {}", request.getHeader("X-User-Email"), role);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        try {
            UserDto createdUser = userService.createUser(userDto);
            log.info("Admin {} created new user: {}", request.getHeader("X-User-Email"), createdUser.getEmail());
            return ResponseEntity.ok(createdUser);
        } catch (Exception e) {
            log.error("Error creating user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        try {
            UserDto updatedUser = userService.updateUser(id, userDto);
            log.info("Admin {} updated user with ID: {}", request.getHeader("X-User-Email"), id);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            log.error("Error updating user: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateOwnProfile(@Valid @RequestBody UserDto userDto, HttpServletRequest request) {
        try {
            // Get current user ID from JWT token headers
            String userIdHeader = request.getHeader("X-User-Id");
            String userEmail = request.getHeader("X-User-Email");
            String userRole = request.getHeader("X-User-Role");
            
            if (userIdHeader == null || userEmail == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required. Missing user information.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
              Long userId = Long.parseLong(userIdHeader);
            
            // Get current user data to preserve certain fields
            Optional<UserDto> currentUserOpt = userService.getUserById(userId);
            if (currentUserOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            UserDto currentUser = currentUserOpt.get();
            
            // For security, ensure certain fields match the authenticated user
            userDto.setUserId(userId);
            userDto.setEmail(userEmail);
            
            // Don't allow role changes through profile update
            if (userRole != null) {
                userDto.setRole(userRole);
            }
            
            // Preserve approval status - users cannot change their own approval status
            userDto.setApproval(currentUser.getApproval());
            
            UserDto updatedUser = userService.updateUser(userId, userDto);
            log.info("User {} updated their own profile", userEmail);
            return ResponseEntity.ok(updatedUser);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID in headers: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid user ID format");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update profile: " + e.getMessage());            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(HttpServletRequest request) {
        try {
            // Get current user ID from JWT token headers
            String userIdHeader = request.getHeader("X-User-Id");
            String userEmail = request.getHeader("X-User-Email");
            
            if (userIdHeader == null || userEmail == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Authentication required. Missing user information.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }
            
            Long userId = Long.parseLong(userIdHeader);
            
            // Get current user data
            Optional<UserDto> currentUserOpt = userService.getUserById(userId);
            if (currentUserOpt.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            UserDto currentUser = currentUserOpt.get();
            log.info("User {} retrieved their profile", userEmail);
            return ResponseEntity.ok(currentUser);
        } catch (NumberFormatException e) {
            log.error("Invalid user ID in headers: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid user ID format");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error retrieving user profile: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to retrieve profile: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        try {
            userService.deleteUser(id);
            log.info("Admin {} deleted user with ID: {}", request.getHeader("X-User-Email"), id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error deleting user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pending-approvals")
    public ResponseEntity<?> getPendingApprovals(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        List<UserDto> pendingUsers = userService.getPendingApprovals();
        log.info("Admin {} retrieved pending approvals", request.getHeader("X-User-Email"));
        return ResponseEntity.ok(pendingUsers);
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        try {
            UserDto approvedUser = userService.approveUser(id);
            log.info("Admin {} approved user with ID: {}", request.getHeader("X-User-Email"), id);
            return ResponseEntity.ok(approvedUser);
        } catch (Exception e) {
            log.error("Error approving user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        try {
            UserDto rejectedUser = userService.rejectUser(id);
            log.info("Admin {} rejected user with ID: {}", request.getHeader("X-User-Email"), id);
            return ResponseEntity.ok(rejectedUser);
        } catch (Exception e) {
            log.error("Error rejecting user: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/count/{role}")
    public ResponseEntity<?> countUsersByRole(@PathVariable String role, HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        long count = userService.countUsersByRole(role);
        log.info("Admin {} retrieved count for role: {}", request.getHeader("X-User-Email"), role);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/count/pending")
    public ResponseEntity<?> countPendingApprovals(HttpServletRequest request) {
        if (!isAdmin(request)) {
            return forbidden();
        }
        
        long count = userService.countPendingApprovals();
        log.info("Admin {} retrieved pending approvals count", request.getHeader("X-User-Email"));
        return ResponseEntity.ok(count);
    }    // Debug endpoint to test header access
    @GetMapping("/debug/headers")
    public ResponseEntity<Map<String, Object>> debugHeaders(HttpServletRequest request) {
        Map<String, Object> debug = new HashMap<>();
        Map<String, String> userInfo = getUserInfo(request);
        
        debug.put("userInfo", userInfo);
        debug.put("isAdmin", isAdmin(request));
        debug.put("requestURI", request.getRequestURI());
        debug.put("method", request.getMethod());
        
        return ResponseEntity.ok(debug);
    }

    // Internal service-to-service endpoint for getting basic user info
    @GetMapping("/internal/{id}")
    public ResponseEntity<Map<String, Object>> getUserForService(@PathVariable Long id) {
        log.debug("Internal service request for user ID: {}", id);
        Optional<UserDto> userOpt = userService.getUserById(id);
        
        if (userOpt.isPresent()) {
            UserDto user = userOpt.get();
            Map<String, Object> response = new HashMap<>();
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", user.getUserId());
            userData.put("name", user.getName());
            userData.put("email", user.getEmail());
            userData.put("contactNumber", user.getContactNumber());
            userData.put("role", user.getRole());
            
            response.put("success", true);
            response.put("data", userData);
            
            log.debug("Internal service retrieved user data for ID: {}", id);
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "User not found");
            return ResponseEntity.notFound().build();
        }
    }
} 