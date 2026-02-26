package com.example.demo.controller;

import com.example.demo.dto.ScheduleDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.services.UserDetailsImpl;
import com.example.demo.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    private final UserRepository userRepository;

    private String currentRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return "USER";
        boolean isAdmin = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
        if (isAdmin) return "ADMIN";
        boolean isTeacher = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_TEACHER"::equals);
        if (isTeacher) return "TEACHER";
        return "USER";
    }

    private boolean isAdminOrTeacher() {
        String role = currentRole();
        return "ADMIN".equals(role) || "TEACHER".equals(role);
    }

    private String currentUserGroupId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return ud.getGroupId();
        }
        String email = auth.getName();
        return userRepository.findByEmail(email).map(User::getGroupId).orElse(null);
    }

    private String currentUserSubject() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return ud.getSubject();
        }
        String email = auth.getName();
        return userRepository.findByEmail(email).map(User::getSubject).orElse(null);
    }

    private List<ScheduleDTO> filterForTeacher(List<ScheduleDTO> schedules) {
        String subject = currentUserSubject();
        if (subject == null || subject.isBlank()) return schedules;
        return schedules.stream()
                .filter(s -> subject.equalsIgnoreCase(s.getSubject()))
                .collect(Collectors.toList());
    }
    
    /**
     * ADMIN: все расписания (можно фильтровать ?groupId=...).
     * TEACHER: все группы, но только его предметы (по полю teacher в расписании), + опц. ?groupId.
     * Ученик: только расписание своей группы.
     */
    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules(
            @RequestParam(required = false) String groupId) {
        try {
            List<ScheduleDTO> schedules = scheduleService.getAllSchedules();
            String role = currentRole();
            switch (role) {
                case "ADMIN":
                    if (groupId != null && !groupId.isBlank()) {
                        schedules = schedules.stream()
                                .filter(s -> groupId.equals(s.getGroupId()))
                                .collect(Collectors.toList());
                    }
                    break;
                case "TEACHER":
                    schedules = filterForTeacher(schedules);
                    if (groupId != null && !groupId.isBlank()) {
                        schedules = schedules.stream()
                                .filter(s -> groupId.equals(s.getGroupId()))
                                .collect(Collectors.toList());
                    }
                    break;
                default:
                    String userGroup = currentUserGroupId();
                    if (userGroup != null && !userGroup.isBlank()) {
                        schedules = schedules.stream()
                                .filter(s -> userGroup.equals(s.getGroupId()))
                                .collect(Collectors.toList());
                    }
                    break;
            }
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/today")
    public ResponseEntity<List<ScheduleDTO>> getTodaySchedule() {
        try {
            List<ScheduleDTO> todaySchedule = scheduleService.getTodaySchedule();
            String role = currentRole();
            if ("TEACHER".equals(role)) {
                todaySchedule = filterForTeacher(todaySchedule);
            } else if ("USER".equals(role)) {
                String userGroup = currentUserGroupId();
                if (userGroup != null && !userGroup.isBlank()) {
                    todaySchedule = todaySchedule.stream()
                            .filter(s -> userGroup.equals(s.getGroupId()))
                            .collect(Collectors.toList());
                }
            }
            return ResponseEntity.ok(todaySchedule);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<ScheduleDTO>> getScheduleByDay(@PathVariable String dayOfWeek) {
        try {
            List<ScheduleDTO> schedule = scheduleService.getScheduleByDay(dayOfWeek);
            String role = currentRole();
            if ("TEACHER".equals(role)) {
                schedule = filterForTeacher(schedule);
            } else if ("USER".equals(role)) {
                String userGroup = currentUserGroupId();
                if (userGroup != null && !userGroup.isBlank()) {
                    schedule = schedule.stream()
                            .filter(s -> userGroup.equals(s.getGroupId()))
                            .collect(Collectors.toList());
                }
            }
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** Расписание по группе. Учитель видит только свой предмет. */
    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ScheduleDTO>> getScheduleByGroup(@PathVariable String groupId) {
        try {
            if (!isAdminOrTeacher()) {
                String userGroup = currentUserGroupId();
                if (userGroup == null || !userGroup.equals(groupId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            List<ScheduleDTO> schedules = scheduleService.getAllSchedules().stream()
                    .filter(s -> groupId.equals(s.getGroupId()))
                    .collect(Collectors.toList());
            if ("TEACHER".equals(currentRole())) {
                schedules = filterForTeacher(schedules);
            }
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleDTO> getScheduleById(@PathVariable String id) {
        try {
            ScheduleDTO schedule = scheduleService.getScheduleById(id);
            return ResponseEntity.ok(schedule);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> createSchedule(@RequestBody java.util.Map<String, Object> request) {
        try {
            log.info("Creating schedule with request: {}", request);
            
            // Handle Postman format: {title, description, startDate, endDate}
            ScheduleDTO createdSchedule = scheduleService.createScheduleFromPostman(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSchedule);
            
        } catch (IllegalArgumentException | IllegalStateException e) {
            log.warn("Validation error creating schedule: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
            
        } catch (Exception e) {
            log.error("Error creating schedule", e);
            String errorMessage = "An error occurred while creating the schedule: " + 
                               (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                             .body(errorMessage);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> updateSchedule(
            @PathVariable("id") String id,
            @RequestBody java.util.Map<String, Object> request) {
        try {
            ScheduleDTO updatedSchedule = scheduleService.updateScheduleFromPostman(id, request);
            return ResponseEntity.ok(updatedSchedule);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the schedule");
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> deleteSchedule(@PathVariable("id") String id) {
        try {
            scheduleService.deleteSchedule(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while deleting the schedule");
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<ScheduleDTO>> searchSchedulesByName(@RequestParam String name) {
        try {
            List<ScheduleDTO> schedules = scheduleService.searchSchedulesByName(name);
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
