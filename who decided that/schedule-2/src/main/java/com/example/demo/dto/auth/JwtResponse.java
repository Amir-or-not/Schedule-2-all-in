package com.example.demo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";

    public JwtResponse(String token, String id, String username, String email, String role,
                       String fullName, String groupId, String createdAt, String subject) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
        this.groupId = groupId;
        this.createdAt = createdAt;
        this.subject = subject;
    }
    private String id;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String groupId;
    private String createdAt;
    private String subject;
}
