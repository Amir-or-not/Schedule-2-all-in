package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

public class AnalyticsDTO {
    
    private Long id;
    
    @NotBlank(message = "Prompt is required")
    private String prompt;
    
    @NotBlank(message = "Answer is required")
    private String answer;
    
    // For Postman compatibility - metricName, metricValue, timestamp
    @JsonProperty("metricName")
    private String metricName;
    
    @JsonProperty("metricValue")
    private Integer metricValue;
    
    @JsonProperty("timestamp")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    private LocalDateTime timestamp;
    
    // Constructors
    public AnalyticsDTO() {}
    
    public AnalyticsDTO(String prompt, String answer) {
        this.prompt = prompt;
        this.answer = answer;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getPrompt() {
        return prompt;
    }
    
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
    
    public String getAnswer() {
        return answer;
    }
    
    public void setAnswer(String answer) {
        this.answer = answer;
    }
    
    public String getMetricName() {
        return metricName != null ? metricName : prompt;
    }
    
    public void setMetricName(String metricName) {
        this.metricName = metricName;
        if (this.prompt == null) {
            this.prompt = metricName;
        }
    }
    
    public Integer getMetricValue() {
        return metricValue;
    }
    
    public void setMetricValue(Integer metricValue) {
        this.metricValue = metricValue;
        if (this.answer == null && metricValue != null) {
            this.answer = String.valueOf(metricValue);
        }
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
