package com.example.demo.config;

import com.example.demo.entity.Group;
import com.example.demo.entity.Schedule;
import com.example.demo.entity.User;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // Create default schedule first
        String defaultScheduleId = createDefaultSchedule();
        
        // Create default groups
        createDefaultGroups(defaultScheduleId);
        
        // Admin user details
        String adminEmail = "admin@example.com";
        String adminId = "admin";
        String adminPassword = "admin123";
        String adminFullName = "Administrator";
        String adminRole = "ADMIN";
        String defaultGroupId = "admin-group";
        
        // Ensure admin user exists with correct credentials
        try {
            // Try to find existing admin by email or user ID
            User admin = userRepository.findByEmail(adminEmail)
                .orElseGet(() -> userRepository.findByUserId(adminId).orElse(null));
            
            if (admin == null) {
                // Create new admin user
                admin = new User();
                admin.setUserId(adminId);
                admin.setEmail(adminEmail);
                admin.setFullName(adminFullName);
                admin.setRole(adminRole);
                admin.setGroupId(defaultGroupId);
                log.info("Creating new admin user...");
            } else {
                log.info("Updating existing admin user...");
            }
            
            // Always update password and other fields to ensure they're correct
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setEmail(adminEmail);
            admin.setRole(adminRole);
            admin.setGroupId(defaultGroupId);
            
            userRepository.save(admin);
            log.info("Admin user is ready - Email: {}, Password: {}", adminEmail, adminPassword);
            
        } catch (Exception e) {
            log.error("Error creating/updating admin user: {}", e.getMessage(), e);
            
            // Try to create admin user using standard JPA methods
            try {
                log.warn("Trying to create admin user with standard JPA...");
                User newAdmin = new User();
                newAdmin.setUserId(adminId);
                newAdmin.setEmail(adminEmail);
                newAdmin.setFullName(adminFullName);
                newAdmin.setPassword(passwordEncoder.encode(adminPassword));
                newAdmin.setRole(adminRole);
                newAdmin.setGroupId(defaultGroupId);
                userRepository.save(newAdmin);
                log.info("Admin user created successfully via standard JPA");
            } catch (Exception ex) {
                log.error("Failed to create admin user: {}", ex.getMessage());
            }
        }
        
        // Check and update admin user if needed
        User adminCheck = userRepository.findByEmail(adminEmail)
            .orElseGet(() -> userRepository.findByUserId(adminId).orElse(null));
            
        if (adminCheck != null) {
            boolean needsUpdate = false;
            
            String currentPassword = adminCheck.getPassword();
            if (currentPassword == null || currentPassword.isEmpty()) {
                adminCheck.setPassword(passwordEncoder.encode(adminPassword));
                needsUpdate = true;
            }
            
            String currentRole = adminCheck.getRole();
            if (currentRole == null || currentRole.isEmpty()) {
                adminCheck.setRole(adminRole);
                needsUpdate = true;
            }
            
            String currentGroupId = adminCheck.getGroupId();
            if (currentGroupId == null || currentGroupId.isEmpty()) {
                adminCheck.setGroupId(defaultGroupId);
                needsUpdate = true;
            }
            
            if (needsUpdate) {
                try {
                    userRepository.save(adminCheck);
                    log.info("Updated admin user with missing fields");
                } catch (Exception e) {
                    log.error("Error updating admin user: {}", e.getMessage(), e);
                }
            }
        }
        
        // Log all users for debugging
        List<User> allUsers = userRepository.findAll();
        if (allUsers.isEmpty()) {
            log.warn("No users found in the database!");
        } else {
            log.info("Current users in database ({}):", allUsers.size());
            allUsers.forEach(user -> 
                log.info("- {} (ID: {}, Email: {}, Role: {}, Group: {})", 
                    user.getFullName(), 
                    user.getUserId(), 
                    user.getEmail(), 
                    user.getRole(), 
                    user.getGroupId())
            );
        }
    }
    
    private String createDefaultSchedule() {
        String defaultScheduleId = "default_schedule";
        if (!scheduleRepository.existsById(defaultScheduleId)) {
            Schedule defaultSchedule = new Schedule();
            defaultSchedule.setScheduleId(defaultScheduleId);
            defaultSchedule.setScheduleName("Default Schedule");
            defaultSchedule.setGroupId("default-group");
            defaultSchedule.setDayOfWeek("MONDAY");
            defaultSchedule.setSubject("Default Subject");
            defaultSchedule.setStartTime(LocalTime.of(9, 0));
            defaultSchedule.setEndTime(LocalTime.of(10, 0));
            Map<String, Object> scheduleData = new HashMap<>();
            scheduleData.put("description", "Default schedule for all groups");
            defaultSchedule.setData(scheduleData);
            scheduleRepository.save(defaultSchedule);
            log.info("Created default schedule: {}", defaultScheduleId);
        }
        return defaultScheduleId;
    }
    
    private void createDefaultGroups(String scheduleId) {
        // Create admin-group if it doesn't exist
        String adminGroupId = "admin-group";
        if (!groupRepository.existsById(adminGroupId)) {
            Group adminGroup = new Group();
            adminGroup.setGroupId(adminGroupId);
            adminGroup.setScheduleId(scheduleId);
            adminGroup.setGroupName("Admin Group");
            adminGroup.setDescription("Administrator group");
            Map<String, Object> adminData = new HashMap<>();
            adminGroup.setData(adminData);
            groupRepository.save(adminGroup);
            log.info("Created admin group: {}", adminGroupId);
        }
        
        // Create user-group if it doesn't exist
        String userGroupId = "user-group";
        if (!groupRepository.existsById(userGroupId)) {
            Group userGroup = new Group();
            userGroup.setGroupId(userGroupId);
            userGroup.setScheduleId(scheduleId);
            userGroup.setGroupName("User Group");
            userGroup.setDescription("Default user group");
            Map<String, Object> userData = new HashMap<>();
            userGroup.setData(userData);
            groupRepository.save(userGroup);
            log.info("Created user group: {}", userGroupId);
        }
    }
}
