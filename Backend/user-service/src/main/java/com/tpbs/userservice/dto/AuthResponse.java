package com.tpbs.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private UserDto user;
    private String message;
    
    // Custom constructors
    public AuthResponse(String token, UserDto user) {
        this.token = token;
        this.user = user;
        this.type = "Bearer";
    }
    
    public AuthResponse(String token, UserDto user, String message) {
        this.token = token;
        this.user = user;
        this.message = message;
        this.type = "Bearer";
    }
} 