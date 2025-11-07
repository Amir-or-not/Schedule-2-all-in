package com.example.demo.service;

import com.example.demo.dto.AnalyticsDTO;
import com.example.demo.dto.FastApiRequest;
import com.example.demo.dto.FastApiResponse;
import com.example.demo.entity.Analytics;
import com.example.demo.repository.AnalyticsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@Transactional
public class AnalyticsService {
    
    @Autowired
    private AnalyticsRepository analyticsRepository;
    
    @Autowired
    private WebClient fastApiWebClient;
    
    public List<AnalyticsDTO> getAllAnalytics() {
        return analyticsRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<AnalyticsDTO> getAnalyticsById(Long id) {
        return analyticsRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public AnalyticsDTO createAnalytics(AnalyticsDTO analyticsDTO) {
        Analytics analytics = convertToEntity(analyticsDTO);
        // If timestamp is provided, set it
        if (analyticsDTO.getTimestamp() != null) {
            analytics.setCreatedAt(analyticsDTO.getTimestamp());
        }
        Analytics savedAnalytics = analyticsRepository.save(analytics);
        return convertToDTO(savedAnalytics);
    }
    
    public Optional<AnalyticsDTO> updateAnalytics(Long id, AnalyticsDTO analyticsDTO) {
        return analyticsRepository.findById(id)
                .map(existingAnalytics -> {
                    existingAnalytics.setPrompt(analyticsDTO.getPrompt());
                    existingAnalytics.setAnswer(analyticsDTO.getAnswer());
                    Analytics updatedAnalytics = analyticsRepository.save(existingAnalytics);
                    return convertToDTO(updatedAnalytics);
                });
    }
    
    public boolean deleteAnalytics(Long id) {
        if (analyticsRepository.existsById(id)) {
            analyticsRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    public List<AnalyticsDTO> searchAnalytics(String searchTerm) {
        return analyticsRepository.findByPromptOrAnswerContaining(searchTerm).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AnalyticsDTO> getAnalyticsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return analyticsRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AnalyticsDTO> getAnalyticsOrderedByDate() {
        return analyticsRepository.findAllOrderByCreatedAtDesc().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    // Student Performance Analysis
    public Map<String, Object> getStudentPerformance(String studentId, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement actual student performance logic
        Map<String, Object> result = new HashMap<>();
        result.put("studentId", studentId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("performance", 85.5); // Example value
        return result;
    }
    
    // Class Performance Analysis
    public Map<String, Object> getClassPerformance(String classId, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement actual class performance logic
        Map<String, Object> result = new HashMap<>();
        result.put("classId", classId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("averageScore", 78.3); // Example value
        return result;
    }
    
    // Attendance Statistics
    public Map<String, Object> getAttendanceStatistics(String studentId, String classId, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement actual attendance statistics logic
        Map<String, Object> result = new HashMap<>();
        if (studentId != null) result.put("studentId", studentId);
        if (classId != null) result.put("classId", classId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("attendanceRate", 92.5); // Example value
        return result;
    }
    
    // Grade Distribution
    public Map<String, Object> getGradeDistribution(String subject, String classId, LocalDate startDate, LocalDate endDate) {
        // TODO: Implement actual grade distribution logic
        Map<String, Object> result = new HashMap<>();
        if (subject != null) result.put("subject", subject);
        if (classId != null) result.put("classId", classId);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        // Example grade distribution
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("A", 15);
        distribution.put("B", 25);
        distribution.put("C", 40);
        distribution.put("D", 15);
        distribution.put("F", 5);
        result.put("distribution", distribution);
        return result;
    }
    
    /**
     * Generate analytics using FastAPI AI service and save to database
     * @param prompt The prompt to send to FastAPI
     * @return AnalyticsDTO with generated response
     */
    public AnalyticsDTO generateAnalyticsFromFastApi(String prompt) {
        try {
            // Call FastAPI service
            FastApiRequest request = new FastApiRequest(prompt);
            
            FastApiResponse response = fastApiWebClient.post()
                    .uri("/analytics/")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(FastApiResponse.class)
                    .block(); // Blocking call for simplicity
            
            if (response == null || response.getText() == null) {
                throw new RuntimeException("FastAPI returned empty response");
            }
            
            // Create AnalyticsDTO from FastAPI response
            AnalyticsDTO analyticsDTO = new AnalyticsDTO();
            analyticsDTO.setPrompt(prompt);
            analyticsDTO.setAnswer(response.getText());
            analyticsDTO.setTimestamp(LocalDateTime.now());
            
            // Save to database
            return createAnalytics(analyticsDTO);
            
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Failed to call FastAPI: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Error generating analytics: " + e.getMessage(), e);
        }
    }
    
    private AnalyticsDTO convertToDTO(Analytics analytics) {
        AnalyticsDTO dto = new AnalyticsDTO(
                analytics.getPrompt(),
                analytics.getAnswer()
        );
        dto.setId(analytics.getId());
        dto.setTimestamp(analytics.getCreatedAt());
        // Map prompt/answer to metricName/metricValue for Postman compatibility
        dto.setMetricName(analytics.getPrompt());
        try {
            dto.setMetricValue(Integer.parseInt(analytics.getAnswer()));
        } catch (NumberFormatException e) {
            // If answer is not a number, metricValue remains null
        }
        return dto;
    }
    
    private Analytics convertToEntity(AnalyticsDTO analyticsDTO) {
        // Handle Postman format: use metricName as prompt, metricValue as answer
        String prompt = analyticsDTO.getMetricName() != null ? 
                analyticsDTO.getMetricName() : analyticsDTO.getPrompt();
        String answer = analyticsDTO.getMetricValue() != null ? 
                String.valueOf(analyticsDTO.getMetricValue()) : analyticsDTO.getAnswer();
        
        if (prompt == null || prompt.isEmpty()) {
            prompt = "default_metric";
        }
        if (answer == null || answer.isEmpty()) {
            answer = "0";
        }
        
        return new Analytics(prompt, answer);
    }
}
