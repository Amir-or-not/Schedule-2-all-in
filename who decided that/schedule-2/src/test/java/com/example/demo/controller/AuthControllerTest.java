package com.example.demo.controller;

import com.example.demotest.ControllerTestApplication;
import com.example.demo.dto.auth.SignInRequest;
import com.example.demo.dto.auth.SignUpRequest;
import com.example.demo.security.services.UserDetailsImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.dto.GroupDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.GroupService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = ControllerTestApplication.class)
@AutoConfigureMockMvc
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private com.example.demo.security.jwt.JwtUtils jwtUtils;

    @MockBean
    private GroupRepository groupRepository;

    @MockBean
    private GroupService groupService;

    @Nested
    @DisplayName("POST /api/auth/signin")
    class SignInTests {

        @Test
        @DisplayName("should signin with valid credentials")
        void shouldSigninWithValidCredentials() throws Exception {
            SignInRequest request = new SignInRequest();
            request.setUsername("user@example.com");
            request.setPassword("password123");

            UserDetailsImpl userDetails = new UserDetailsImpl(
                    "user-1",
                    "Test User",
                    "user@example.com",
                    "encoded",
                    "group-1",
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
            );

            Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").exists())
                    .andExpect(jsonPath("$.email").value("user@example.com"));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }

        @Test
        @DisplayName("should return 401 with invalid credentials")
        void shouldReturn401WithInvalidCredentials() throws Exception {
            SignInRequest request = new SignInRequest();
            request.setUsername("user@example.com");
            request.setPassword("wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/auth/signin")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Authentication failed")));

            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/signup")
    class SignUpTests {

        @Test
        @DisplayName("should signup with new user")
        void shouldSignupWithNewUser() throws Exception {
            SignUpRequest request = new SignUpRequest();
            request.setUsername("newuser");
            request.setEmail("new@example.com");
            request.setPassword("password123");
            request.setFullName("New User");
            request.setGroupId("user-group");

            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(groupRepository.existsById("user-group")).thenReturn(true);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("User registered successfully")));

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("should return 400 when email already exists")
        void shouldReturn400WhenEmailExists() throws Exception {
            SignUpRequest request = new SignUpRequest();
            request.setUsername("existinguser");
            request.setEmail("existing@example.com");
            request.setPassword("password123");
            request.setFullName("Existing User");
            request.setGroupId("user-group");

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("Email is already in use")));

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("GET /api/auth/groups")
    class GetGroupsForRegistrationTests {

        @Test
        @DisplayName("should return groups for registration")
        void shouldReturnGroupsForRegistration() throws Exception {
            GroupDTO group = new GroupDTO();
            group.setGroupId("user-group");
            group.setGroupName("User Group");
            group.setScheduleId("default_schedule");
            when(groupService.getAllGroups()).thenReturn(List.of(group));

            mockMvc.perform(get("/api/auth/groups"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].groupId").value("user-group"))
                    .andExpect(jsonPath("$[0].groupName").value("User Group"));

            verify(groupService).getAllGroups();
        }
    }
}
