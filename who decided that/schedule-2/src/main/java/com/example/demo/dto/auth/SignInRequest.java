package com.example.demo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignInRequest {
    @NotBlank(message = "Username or email is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
    
    // For backward compatibility, support both username and email
    public String getEmail() {
        return username; // username can be email
    }
    
    public String getUsername() {
        return username;
    }
}
