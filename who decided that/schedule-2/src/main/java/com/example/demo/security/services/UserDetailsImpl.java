package com.example.demo.security.services;

import com.example.demo.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private String id;
    private String fullName;
    private String email;
    @JsonIgnore
    private String password;
    private String groupId;
    private String subject;
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(String id, String fullName, String email, String password, 
                          String groupId, String subject, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.groupId = groupId;
        this.subject = subject;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        String role = (user.getRole() != null && !user.getRole().trim().isEmpty()) 
            ? user.getRole().trim().toUpperCase()
            : "USER";
        if (role.startsWith("ROLE_")) role = role.substring(5);
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));

        if (role.contains("TEACHER") && !"TEACHER".equals(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TEACHER"));
        }
        if ("ADMIN".equals(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TEACHER"));
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        
        String subject = user.getSubject();
        if ((subject == null || subject.isBlank()) && role.contains("TEACHER") && !"TEACHER".equals(role)) {
            subject = resolveSubjectFromRole(role);
        }

        return new UserDetailsImpl(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getPassword(),
                user.getGroupId(),
                subject,
                authorities
        );
    }

    private static final Map<String, String> ROLE_SUBJECT_MAP = new LinkedHashMap<>();
    static {
        ROLE_SUBJECT_MAP.put("MATH", "Математика");
        ROLE_SUBJECT_MAP.put("PHYSICS", "Физика");
        ROLE_SUBJECT_MAP.put("HISTORY", "История");
        ROLE_SUBJECT_MAP.put("RUSSIAN", "Русский язык");
        ROLE_SUBJECT_MAP.put("INFORM", "Информатика");
        ROLE_SUBJECT_MAP.put("CS", "Информатика");
        ROLE_SUBJECT_MAP.put("ENGLISH", "Английский язык");
        ROLE_SUBJECT_MAP.put("BIO", "Биология");
        ROLE_SUBJECT_MAP.put("CHEM", "Химия");
        ROLE_SUBJECT_MAP.put("GEO", "География");
        ROLE_SUBJECT_MAP.put("LIT", "Литература");
    }

    public static String resolveSubjectFromRole(String role) {
        if (role == null) return null;
        String upper = role.toUpperCase();
        for (Map.Entry<String, String> e : ROLE_SUBJECT_MAP.entrySet()) {
            if (upper.contains(e.getKey())) return e.getValue();
        }
        return null;
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

    public String getSubject() {
        return subject;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
