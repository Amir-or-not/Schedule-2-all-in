package com.example.demo.dto.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class CreateScheduleRequest {
    @NotBlank(message = "Day of week is required")
    private String dayOfWeek;
    
    @NotBlank(message = "Subject is required")
    private String subject;
    
    @NotNull(message = "Start time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;
    
    private String room;
    private String teacher;
    private String type;
}
