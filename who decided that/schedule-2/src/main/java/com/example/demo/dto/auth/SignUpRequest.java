package com.example.demo.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class SignUpRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Group ID is required")
    private String groupId;

    // Role is optional - can be a single string or list
    private String role;
    private List<String> roles;

    public String getFirstRole() {
        if (role != null && !role.isEmpty()) {
            return role.startsWith("ROLE_") ? role : "ROLE_" + role;
        }
        if (roles != null && !roles.isEmpty()) {
            String firstRole = roles.get(0);
            return firstRole.startsWith("ROLE_") ? firstRole : "ROLE_" + firstRole;
        }
        return "ROLE_USER";
    }
}
