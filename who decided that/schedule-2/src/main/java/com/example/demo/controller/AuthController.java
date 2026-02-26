package com.example.demo.controller;

import com.example.demo.dto.GroupDTO;
import com.example.demo.dto.auth.JwtResponse;
import com.example.demo.dto.auth.SignInRequest;
import com.example.demo.dto.auth.SignUpRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.GroupService;
import com.example.demo.security.jwt.JwtUtils;
import com.example.demo.security.services.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    GroupService groupService;

    /**
     * Список групп для выбора при регистрации (доступен без авторизации).
     */
    @GetMapping("/groups")
    public ResponseEntity<List<GroupDTO>> getGroupsForRegistration() {
        List<GroupDTO> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody SignInRequest loginRequest) {
        try {
            // Authenticate user - support both username and email
            // Try username first, then try as email
            String usernameOrEmail = loginRequest.getUsername();
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    usernameOrEmail,
                    loginRequest.getPassword()
                )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            log.info("[AUTH] signin success: user={}, authorities={}", userDetails.getUsername(), userDetails.getAuthorities());

            // Generate JWT token with roles
            String roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

            // Create JWT token manually to ensure roles are included
            String jwt = Jwts.builder()
                .setSubject(userDetails.getUsername())
                .claim("roles", roles)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24 hours
                .signWith(Keys.hmacShaKeyFor("your-secret-key-change-this-in-production".getBytes()), 
                         SignatureAlgorithm.HS256)
                .compact();

            String fullName = userDetails.getFullName();
            String groupId = userDetails.getGroupId();
            String createdAt = null;
            String subject = userDetails.getSubject();
            try {
                User dbUser = userRepository.findByEmail(userDetails.getEmail()).orElse(null);
                if (dbUser != null) {
                    if (dbUser.getFullName() != null) fullName = dbUser.getFullName();
                    groupId = dbUser.getGroupId();
                    if (dbUser.getSubject() != null && !dbUser.getSubject().isBlank()) {
                        subject = dbUser.getSubject();
                    }
                    if (dbUser.getCreatedAt() != null) {
                        createdAt = dbUser.getCreatedAt().toString();
                    }
                }
            } catch (Exception ignored) {}

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    fullName,
                    userDetails.getEmail(),
                    roles,
                    fullName,
                    groupId,
                    createdAt,
                    subject
            ));
        } catch (Exception e) {
            log.warn("[AUTH] signin failed: usernameOrEmail={}, error={}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        String email = signUpRequest.getEmail();
        boolean emailExists = userRepository.existsByEmail(email);
        log.info("[AUTH] signup request: email={}, existsByEmail={}, username={}, fullName={}", email, emailExists, signUpRequest.getUsername(), signUpRequest.getFullName());
        if (emailExists) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Нормализация и проверка группы: при пустом значении подставляем user-group
        String groupId = signUpRequest.getGroupId();
        if (groupId == null || groupId.trim().isEmpty() || "default-group".equals(groupId)) {
            groupId = "user-group";
        } else {
            groupId = groupId.trim();
        }

        // Проверка существования группы в БД — без неё не создаём пользователя
        if (!groupRepository.existsById(groupId)) {
            log.warn("[AUTH] signup rejected: group not found, groupId={}", groupId);
            return ResponseEntity
                    .badRequest()
                    .body("Error: Group not found. Please choose a group from the list (use GET /api/auth/groups).");
        }

        // Create new user's account
        User user = new User(
                java.util.UUID.randomUUID().toString(),
                signUpRequest.getFullName(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getEmail(),
                groupId,
                signUpRequest.getFirstRole()
        );

        try {
            userRepository.save(user);
            log.info("[AUTH] signup success: email={}, groupId={}", signUpRequest.getEmail(), groupId);
            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            log.error("[AUTH] signup failed: email={}", signUpRequest.getEmail(), e);
            return ResponseEntity.status(500)
                    .body("Error: An unexpected error occurred: " + e.getMessage());
        }
    }
}
