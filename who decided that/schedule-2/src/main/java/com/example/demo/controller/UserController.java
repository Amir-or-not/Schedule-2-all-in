package com.example.demo.controller;

import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserDTO;
import com.example.demo.security.services.UserDetailsImpl;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Профиль текущего пользователя (доступен всем авторизованным). */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        log.debug("GET /me - getCurrentUser()");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            log.warn("GET /me - unauthorized: principal is not UserDetailsImpl");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        Optional<UserDTO> user = userService.getUserById(ud.getId());
        if (user.isEmpty()) {
            user = userService.getUserByEmail(ud.getEmail());
        }
        if (user.isPresent()) {
            log.info("GET /me - profile loaded for user={}", ud.getEmail());
        } else {
            log.warn("GET /me - user not found for id={}, email={}", ud.getId(), ud.getEmail());
        }
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /** Список всех пользователей — только ADMIN */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.debug("GET / - getAllUsers()");
        List<UserDTO> users = userService.getAllUsers();
        log.info("GET / - returned {} users", users.size());
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("userId") String userId) {
        log.debug("GET /{} - getUserById(userId={})", userId, userId);
        Optional<UserDTO> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable("email") String email) {
        log.debug("GET /email/{} - getUserByEmail(email={})", email, email);
        Optional<UserDTO> user = userService.getUserByEmail(email);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /** Ученики группы — только учитель/админ. */
    @GetMapping("/group/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<UserDTO>> getUsersByGroupId(@PathVariable("groupId") String groupId) {
        log.debug("GET /group/{} - getUsersByGroupId(groupId={})", groupId, groupId);
        List<UserDTO> users = userService.getUsersByGroupId(groupId);
        log.info("GET /group/{} - returned {} users", groupId, users.size());
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getUsersByRole(@PathVariable("role") String role) {
        log.debug("GET /role/{} - getUsersByRole(role={})", role, role);
        List<UserDTO> users = userService.getUsersByRole(role);
        log.info("GET /role/{} - returned {} users", role, users.size());
        return ResponseEntity.ok(users);
    }
    
    /** Создание пользователя — только ADMIN */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) {
        log.debug("POST / - createUser(userId={}, email={})", userDTO.getUserId(), userDTO.getEmail());
        try {
            if (userDTO.getUserId() == null || userDTO.getUserId().trim().isEmpty()) {
                log.warn("POST / - validation failed: User ID is required");
                return ResponseEntity.badRequest().body("User ID is required");
            }
            if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
                log.warn("POST / - validation failed: Email is required");
                return ResponseEntity.badRequest().body("Email is required");
            }
            
            UserDTO createdUser = userService.createUser(userDTO);
            log.info("POST / - user created: userId={}, email={}", createdUser.getUserId(), createdUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            log.warn("POST / - bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("POST / - error creating user: userId={}, email={}", userDTO.getUserId(), userDTO.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred: " + e.getMessage());
        }
    }
    
    /** Изменение пользователя (роль, группа и т.д.) — только ADMIN */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(
            @PathVariable("userId") String userId,
            @Valid @RequestBody UserDTO userDTO) {
        log.debug("PUT /{} - updateUser(userId={})", userId, userId);
        try {
            if (!userId.equals(userDTO.getUserId())) {
                log.warn("PUT /{} - path/body userId mismatch", userId);
                return ResponseEntity.badRequest().body("User ID in path does not match user ID in request body");
            }
            
            UserDTO updatedUser = userService.updateUser(userId, userDTO)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
            log.info("PUT /{} - user updated successfully", userId);
            return ResponseEntity.ok(updatedUser);
            
        } catch (IllegalArgumentException e) {
            log.warn("PUT /{} - bad request: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("PUT /{} - error updating user", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the user: " + e.getMessage());
        }
    }
    
    /** Частичное обновление (роль, группа, имя, email) без пароля — только ADMIN */
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> patchUser(
            @PathVariable("userId") String userId,
            @RequestBody UpdateUserRequest request) {
        log.debug("PATCH /{} - patchUser(userId={})", userId, userId);
        try {
            Optional<UserDTO> updated = userService.updateUserPartial(
                    userId,
                    request.getFullName(),
                    request.getEmail(),
                    request.getGroupId(),
                    request.getRole(),
                    request.getSubject()
            );
            if (updated.isPresent()) {
                log.info("PATCH /{} - user updated", userId);
            } else {
                log.warn("PATCH /{} - user not found", userId);
            }
            return updated.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.warn("PATCH /{} - bad request: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Удаление пользователя — только ADMIN */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable("userId") String userId) {
        log.debug("DELETE /{} - deleteUser(userId={})", userId, userId);
        try {
            boolean deleted = userService.deleteUser(userId);
            if (deleted) {
                log.info("DELETE /{} - user deleted", userId);
                return ResponseEntity.noContent().build();
            }
            log.warn("DELETE /{} - user not found", userId);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("DELETE /{} - error deleting user", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<UserDTO>> searchUsersByName(@RequestParam("name") String name) {
        log.debug("GET /search?name={} - searchUsersByName", name);
        List<UserDTO> users = userService.searchUsersByName(name);
        log.info("GET /search - query='{}' returned {} users", name, users.size());
        return ResponseEntity.ok(users);
    }

    /** Обновление собственного профиля (имя, email) — доступен всем авторизованным. */
    @PatchMapping("/me")
    public ResponseEntity<?> updateOwnProfile(@RequestBody UpdateUserRequest request) {
        log.debug("PATCH /me - updateOwnProfile()");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            log.warn("PATCH /me - unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        try {
            Optional<UserDTO> updated = userService.updateUserPartial(
                    ud.getId(),
                    request.getFullName(),
                    request.getEmail(),
                    null,
                    null,
                    null
            );
            if (updated.isPresent()) {
                log.info("PATCH /me - profile updated for user={}", ud.getEmail());
            }
            return updated.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.warn("PATCH /me - bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /** Смена пароля текущего пользователя */
    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(@RequestBody java.util.Map<String, String> request) {
        log.debug("POST /me/change-password - changePassword()");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            log.warn("POST /me/change-password - unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        UserDetailsImpl ud = (UserDetailsImpl) auth.getPrincipal();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        if (oldPassword == null || newPassword == null || newPassword.length() < 6) {
            log.warn("POST /me/change-password - validation failed (short or missing password)");
            return ResponseEntity.badRequest().body("Новый пароль должен содержать минимум 6 символов");
        }
        boolean changed = userService.changePassword(ud.getId(), oldPassword, newPassword);
        if (changed) {
            log.info("POST /me/change-password - password changed for user={}", ud.getEmail());
            return ResponseEntity.ok(java.util.Map.of("message", "Пароль успешно изменён"));
        }
        log.warn("POST /me/change-password - wrong current password for user={}", ud.getEmail());
        return ResponseEntity.badRequest().body("Неверный текущий пароль");
    }
}
