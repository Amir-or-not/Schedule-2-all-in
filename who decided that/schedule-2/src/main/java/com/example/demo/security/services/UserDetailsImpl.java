package com.example.demo.security.services;

import com.example.demo.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private String id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private String groupId;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String id, String username, String email, String password, 
                          String groupId, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.groupId = groupId;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        // Get the role from user or default to "USER"
        String role = (user.getRole() != null && !user.getRole().trim().isEmpty()) 
            ? user.getRole() 
            : "USER";
        
        // Ensure the role has the ROLE_ prefix
        String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        
        // Log user details for debugging
        System.out.println("Building UserDetails for user: " + user.getEmail());
        System.out.println("User role from DB: " + user.getRole());
        System.out.println("Normalized role: " + normalizedRole);
        
        // Create authorities - for admin, add all permissions
        Collection<SimpleGrantedAuthority> authorities;
        if ("ROLE_ADMIN".equals(normalizedRole)) {
            // Admin has all permissions
            authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
            System.out.println("Admin user detected, granting all permissions");
        } else {
            // Regular user with their specific role
            authorities = Collections.singletonList(new SimpleGrantedAuthority(normalizedRole));
        }
            
        System.out.println("Authorities: " + authorities);
        
        return new UserDetailsImpl(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getGroupId(),
                authorities
        );
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
