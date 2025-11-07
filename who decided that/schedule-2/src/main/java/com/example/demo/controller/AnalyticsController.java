package com.example.demo.controller;

import com.example.demo.dto.AnalyticsDTO;
import com.example.demo.service.AnalyticsService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @GetMapping
    public ResponseEntity<List<AnalyticsDTO>> getAllAnalytics() {
        List<AnalyticsDTO> analytics = analyticsService.getAllAnalytics();
        return ResponseEntity.ok(analytics);
    }
    
    // Student Performance Analysis
    @GetMapping("/student/{studentId}/performance")
    public ResponseEntity<Map<String, Object>> getStudentPerformance(
            @PathVariable String studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> performance = analyticsService.getStudentPerformance(studentId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }

    // Class Performance Analysis
    @GetMapping("/class/{classId}/performance")
    public ResponseEntity<Map<String, Object>> getClassPerformance(
            @PathVariable String classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> performance = analyticsService.getClassPerformance(classId, startDate, endDate);
        return ResponseEntity.ok(performance);
    }

    // Attendance Statistics
    @GetMapping("/attendance/statistics")
    public ResponseEntity<Map<String, Object>> getAttendanceStatistics(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> stats = analyticsService.getAttendanceStatistics(studentId, classId, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    // Grade Distribution
    @GetMapping("/grades/distribution")
    public ResponseEntity<Map<String, Object>> getGradeDistribution(
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> distribution = analyticsService.getGradeDistribution(subject, classId, startDate, endDate);
        return ResponseEntity.ok(distribution);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnalyticsDTO> getAnalyticsById(@PathVariable Long id) {
        Optional<AnalyticsDTO> analytics = analyticsService.getAnalyticsById(id);
        return analytics.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<AnalyticsDTO> createAnalytics(@Valid @RequestBody AnalyticsDTO analyticsDTO) {
        try {
            AnalyticsDTO createdAnalytics = analyticsService.createAnalytics(analyticsDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAnalytics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AnalyticsDTO> updateAnalytics(@PathVariable Long id, 
                                                      @Valid @RequestBody AnalyticsDTO analyticsDTO) {
        try {
            Optional<AnalyticsDTO> updatedAnalytics = analyticsService.updateAnalytics(id, analyticsDTO);
            return updatedAnalytics.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnalytics(@PathVariable Long id) {
        boolean deleted = analyticsService.deleteAnalytics(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<AnalyticsDTO>> searchAnalytics(@RequestParam String searchTerm) {
        List<AnalyticsDTO> analytics = analyticsService.searchAnalytics(searchTerm);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<AnalyticsDTO>> getAnalyticsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AnalyticsDTO> analytics = analyticsService.getAnalyticsByDateRange(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/ordered")
    public ResponseEntity<List<AnalyticsDTO>> getAnalyticsOrderedByDate() {
        List<AnalyticsDTO> analytics = analyticsService.getAnalyticsOrderedByDate();
        return ResponseEntity.ok(analytics);
    }
    
    @PostMapping("/generate")
    public ResponseEntity<?> generateAnalytics(@RequestBody GenerateAnalyticsRequest request) {
        try {
            if (request.getPrompt() == null || request.getPrompt().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("{\"error\": \"Prompt is required\"}");
            }
            
            AnalyticsDTO generatedAnalytics = analyticsService.generateAnalyticsFromFastApi(request.getPrompt());
            return ResponseEntity.status(HttpStatus.CREATED).body(generatedAnalytics);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"An unexpected error occurred: " + e.getMessage() + "\"}");
        }
    }
    
    // Inner class for request body
    private static class GenerateAnalyticsRequest {
        private String prompt;
        
        public String getPrompt() {
            return prompt;
        }
        
        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }
}
