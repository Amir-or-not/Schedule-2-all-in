package com.example.demo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
// import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @Column(name = "user_id", columnDefinition = "text")
    private String userId;
    
    @Column(name = "full_name", columnDefinition = "text")
    private String fullName;
    
    @Column(name = "password", length = 255)
    private String password;
    
    @Column(name = "email", length = 255)
    private String email;
    
    @Column(name = "group_id", length = 20)
    private String groupId;
    
    @Column(name = "role", length = 20, nullable = false)
    private String role = "USER";  // Default role is USER
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "personal_permissions", columnDefinition = "text")
    private String personalPermissions;
    
    @Column(name = "avatar", length = 255)
    private String avatar;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", insertable = false, updatable = false)
    private Group group;
    
    // Constructors
    public User() {}
    
    public User(String userId, String fullName, String password, String email, String groupId, String role) {
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
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
    
    public Group getGroup() {
        return group;
    }
    
    public void setGroup(Group group) {
        this.group = group;
    }
}
