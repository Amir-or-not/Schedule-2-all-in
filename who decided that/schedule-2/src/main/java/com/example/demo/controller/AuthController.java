package com.example.demo.controller;

import com.example.demo.dto.auth.JwtResponse;
import com.example.demo.dto.auth.SignInRequest;
import com.example.demo.dto.auth.SignUpRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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

            // Log user details for debugging
            System.out.println("User authenticated: " + userDetails.getUsername());
            System.out.println("User authorities: " + userDetails.getAuthorities());

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

            // Log the generated JWT for debugging
            System.out.println("Generated JWT: " + jwt);
            
            // Decode and log the JWT payload for verification
            String[] chunks = jwt.split("\\.");
            if (chunks.length > 1) {
                String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
                System.out.println("JWT Payload: " + payload);
            }

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    roles
            ));
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(401).body("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body("Error: Email is already in use!");
        }

        // Ensure group exists, if not use default user-group
        String groupId = signUpRequest.getGroupId();
        if (groupId == null || groupId.trim().isEmpty()) {
            groupId = "user-group";
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
            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            System.err.println("Registration error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body("Error: An unexpected error occurred: " + e.getMessage());
        }
    }
}
