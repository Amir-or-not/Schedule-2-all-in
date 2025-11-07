package com.example.demo.service;

import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        // Check if user with same ID or email already exists
        if (userRepository.existsById(userDTO.getUserId())) {
            throw new IllegalArgumentException("User with ID " + userDTO.getUserId() + " already exists");
        }
        
        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("User with email " + userDTO.getEmail() + " already exists");
        }
        
        // Encode password
        if (userDTO.getPassword() != null) {
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        
        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
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
                existingUser.setRole(userDTO.getRole());
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
            
            // Update password if provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }
            
            User updatedUser = userRepository.save(existingUser);
            return convertToDTO(updatedUser);
        });
    }
    
    public boolean deleteUser(String userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
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
                "", // Don't return password in DTO
                user.getEmail(),
                user.getGroupId(),
                user.getRole()
        );
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
        user.setPersonalPermissions(userDTO.getPersonalPermissions());
        user.setAvatar(userDTO.getAvatar());
        return user;
    }
}
