package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GradeDTO {
    
    private Long id;
    
    @NotBlank(message = "Student ID is required")
    private String studentId;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotNull(message = "Grade value is required")
    @Min(value = 1, message = "Grade must be at least 1")
    @Max(value = 100, message = "Grade must be at most 100")
    private Integer gradeValue;
    
    private String gradeType; // "exam", "test", "homework", "quiz", "final"
    
    private String teacherId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lessonDate;
    
    private String comment;
    
    private String groupId;
    
    private String scheduleId; // Привязка к уроку в расписании
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}

