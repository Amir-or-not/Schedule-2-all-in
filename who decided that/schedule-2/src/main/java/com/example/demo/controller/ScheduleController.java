package com.example.demo.controller;

import com.example.demo.dto.ScheduleDTO;
import com.example.demo.dto.schedule.ScheduleRequest;
import com.example.demo.dto.schedule.CreateScheduleRequest;
import com.example.demo.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {
    
    private final ScheduleService scheduleService;
    
    @GetMapping
    public ResponseEntity<List<ScheduleDTO>> getAllSchedules() {
        try {
            List<ScheduleDTO> schedules = scheduleService.getAllSchedules();
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/today")
    public ResponseEntity<List<ScheduleDTO>> getTodaySchedule() {
        try {
            List<ScheduleDTO> todaySchedule = scheduleService.getTodaySchedule();
            return ResponseEntity.ok(todaySchedule);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/day/{dayOfWeek}")
    public ResponseEntity<List<ScheduleDTO>> getScheduleByDay(@PathVariable String dayOfWeek) {
        try {
            List<ScheduleDTO> schedule = scheduleService.getScheduleByDay(dayOfWeek);
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
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
