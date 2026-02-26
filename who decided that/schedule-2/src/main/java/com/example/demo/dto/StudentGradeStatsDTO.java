package com.example.demo.dto;

import lombok.Data;

import java.util.List;
// import java.util.Map;

@Data
public class StudentGradeStatsDTO {
    
    private String studentId;
    private String studentName;
    private Double overallAverage; 
    private Integer totalGrades; 
    private List<SubjectGradeStatsDTO> subjectStats; 
    
    @Data
    public static class SubjectGradeStatsDTO {
        private String subject;
        private Double averageGrade; // Средний балл по предмету
        private Integer gradeCount; // Количество оценок
        private List<GradeDTO> grades; // Все оценки по предмету
        private Integer minGrade; // Минимальная оценка
        private Integer maxGrade; // Максимальная оценка
    }
}

