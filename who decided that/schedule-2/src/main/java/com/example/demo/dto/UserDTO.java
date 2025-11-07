package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDTO {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Full name is required")
    private String fullName;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String email;
    
    @Size(max = 20, message = "Group ID must not exceed 20 characters")
    private String groupId;
    
    @Size(max = 20, message = "Role must not exceed 20 characters")
    private String role;
    
    private String personalPermissions;
    
    @Size(max = 255, message = "Avatar URL must not exceed 255 characters")
    private String avatar;
    
    // Constructors
    public UserDTO() {}
    
    public UserDTO(String userId, String fullName, String password, String email, String groupId, String role) {
        this.userId = userId;
        this.fullName = fullName;
        this.password = password;
        this.email = email;
        this.groupId = groupId;
        this.role = role;
    }
    
    // Getters and Setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPersonalPermissions() {
        return personalPermissions;
    }
    
    public void setPersonalPermissions(String personalPermissions) {
        this.personalPermissions = personalPermissions;
    }
    
    public String getAvatar() {
        return avatar;
    }
    
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
