package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, 
                      GroupRepository groupRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<UserDTO> getUserById(String userId) {
        return userRepository.findByUserId(userId)
                .map(this::convertToDTO);
    }
    
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }
    
    public List<UserDTO> getUsersByGroupId(String groupId) {
        return userRepository.findByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<UserDTO> getUsersByRole(String role) {
        return userRepository.findByRole(role).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public UserDTO createUser(UserDTO userDTO) {
        log.debug("createUser(userId={}, email={})", userDTO.getUserId(), userDTO.getEmail());
        if (userRepository.existsById(userDTO.getUserId())) {
            log.warn("createUser failed: userId already exists: {}", userDTO.getUserId());
            throw new IllegalArgumentException("User with ID " + userDTO.getUserId() + " already exists");
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            log.warn("createUser failed: email already exists: {}", userDTO.getEmail());
            throw new IllegalArgumentException("User with email " + userDTO.getEmail() + " already exists");
        }
        
        if (userDTO.getPassword() != null) {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        log.info("[DATA] User created: userId={}, email={}", savedUser.getUserId(), savedUser.getEmail());
        return convertToDTO(savedUser);
    }
    
    public Optional<UserDTO> updateUser(String userId, UserDTO userDTO) {
        if (!userId.equals(userDTO.getUserId())) {
            throw new IllegalArgumentException("User ID in path does not match user ID in request body");
        }
        
        return userRepository.findById(userId).map(existingUser -> {
            // Check if email is being changed and if it already exists
            if (userDTO.getEmail() != null && !userDTO.getEmail().equals(existingUser.getEmail())) {
                if (userRepository.existsByEmail(userDTO.getEmail())) {
                    throw new IllegalArgumentException("User with email " + userDTO.getEmail() + " already exists");
                }
                existingUser.setEmail(userDTO.getEmail());
            }
            
            // Update other fields if provided
            if (userDTO.getFullName() != null) {
                existingUser.setFullName(userDTO.getFullName());
            }
            
            if (userDTO.getRole() != null) {
                String cleanRole = userDTO.getRole().trim().toUpperCase();
                if (cleanRole.startsWith("ROLE_")) cleanRole = cleanRole.substring(5);
                existingUser.setRole(cleanRole);
            }
            
            if (userDTO.getGroupId() != null) {
                // Verify group exists
                if (!groupRepository.existsById(userDTO.getGroupId())) {
                    throw new IllegalArgumentException("Group with ID " + userDTO.getGroupId() + " does not exist");
                }
                existingUser.setGroupId(userDTO.getGroupId());
            }
            
            // Update personal permissions if provided
            if (userDTO.getPersonalPermissions() != null) {
                existingUser.setPersonalPermissions(userDTO.getPersonalPermissions());
            }
            
            // Update avatar if provided
            if (userDTO.getAvatar() != null) {
                existingUser.setAvatar(userDTO.getAvatar());
            }
            
            if (userDTO.getSubject() != null) {
                existingUser.setSubject(userDTO.getSubject());
            }

            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
            
            User updatedUser = userRepository.save(existingUser);
            log.info("[DATA] User updated: userId={}", userId);
            return convertToDTO(updatedUser);
        });
    }
    
    /**
     * Частичное обновление пользователя (роль, группа, имя, email) без смены пароля.
     * Обновляются только переданные непустые поля.
     */
    public Optional<UserDTO> updateUserPartial(String userId, String fullName, String email, String groupId, String role, String subject) {
        log.debug("updateUserPartial(userId={}, fullName={}, email={}, role={})", userId, fullName != null, email != null, role);
        return userRepository.findById(userId).map(existingUser -> {
            if (fullName != null && !fullName.isBlank()) {
                existingUser.setFullName(fullName.trim());
            }
            if (email != null && !email.isBlank()) {
                if (userRepository.existsByEmail(email) && !email.equals(existingUser.getEmail())) {
                    log.warn("updateUserPartial failed: email already exists: {}", email);
                    throw new IllegalArgumentException("User with email " + email + " already exists");
                }
                existingUser.setEmail(email.trim());
            }
            if (groupId != null) {
                if (!groupId.isBlank() && !groupRepository.existsById(groupId)) {
                    log.warn("updateUserPartial failed: group not found: {}", groupId);
                    throw new IllegalArgumentException("Group with ID " + groupId + " does not exist");
                }
                existingUser.setGroupId(groupId.isBlank() ? null : groupId.trim());
            }
            if (role != null && !role.isBlank()) {
                String cleanRole = role.trim().toUpperCase();
                if (cleanRole.startsWith("ROLE_")) cleanRole = cleanRole.substring(5);
                existingUser.setRole(cleanRole);
            }
            if (subject != null) {
                existingUser.setSubject(subject.isBlank() ? null : subject.trim());
            }
            if ((existingUser.getSubject() == null || existingUser.getSubject().isBlank())
                    && existingUser.getRole() != null && existingUser.getRole().contains("TEACHER")
                    && !"TEACHER".equals(existingUser.getRole())) {
                String derived = UserDetailsImpl.resolveSubjectFromRole(existingUser.getRole());
                if (derived != null) existingUser.setSubject(derived);
            }
            User updated = userRepository.save(existingUser);
            log.info("[DATA] User updated (partial): userId={}, role={}, subject={}", userId, role, subject);
            return convertToDTO(updated);
        });
    }

    public boolean changePassword(String userId, String oldPassword, String newPassword) {
        log.debug("changePassword(userId={})", userId);
        return userRepository.findById(userId).map(user -> {
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                log.warn("changePassword failed: wrong current password for userId={}", userId);
                return false;
            }
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            log.info("[DATA] Password changed: userId={}", userId);
            return true;
        }).orElseGet(() -> {
            log.warn("changePassword failed: user not found, userId={}", userId);
            return false;
        });
    }

    public boolean deleteUser(String userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            log.info("[DATA] User deleted: userId={}", userId);
            return true;
        }
        return false;
    }
    
    public List<UserDTO> searchUsersByName(String name) {
        return userRepository.findByFullNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO(
                user.getUserId(),
                user.getFullName(),
                "",
                user.getEmail(),
                user.getGroupId(),
                user.getRole()
        );
        userDTO.setSubject(user.getSubject());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setPersonalPermissions(user.getPersonalPermissions());
        userDTO.setAvatar(user.getAvatar());
        return userDTO;
    }
    
    private User convertToEntity(UserDTO userDTO) {
        User user = new User(
                userDTO.getUserId(),
                userDTO.getFullName(),
                userDTO.getPassword(),
                userDTO.getEmail(),
                userDTO.getGroupId(),
                userDTO.getRole()
        );
        user.setSubject(userDTO.getSubject());
        user.setPersonalPermissions(userDTO.getPersonalPermissions());
        user.setAvatar(userDTO.getAvatar());
        return user;
    }
}
