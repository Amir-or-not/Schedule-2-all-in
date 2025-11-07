package com.example.demo.service;

import com.example.demo.dto.GradeDTO;
import com.example.demo.dto.StudentGradeStatsDTO;
import com.example.demo.entity.Grade;
import com.example.demo.repository.GradeRepository;
import com.example.demo.repository.ScheduleRepository;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class GradeService {
    
    @Autowired
    private GradeRepository gradeRepository;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ScheduleRepository scheduleRepository;
    
    public List<GradeDTO> getAllGrades() {
        return gradeRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<GradeDTO> getGradeById(Long id) {
        return gradeRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public List<GradeDTO> getGradesByStudentId(String studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<GradeDTO> getGradesByGroupId(String groupId) {
        return gradeRepository.findByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<GradeDTO> getGradesByStudentAndSubject(String studentId, String subject) {
        return gradeRepository.findByStudentIdAndSubject(studentId, subject).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<GradeDTO> getGradesByScheduleId(String scheduleId) {
        return gradeRepository.findByScheduleId(scheduleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<GradeDTO> getGradesByStudentAndSchedule(String studentId, String scheduleId) {
        return gradeRepository.findByStudentIdAndScheduleId(studentId, scheduleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public GradeDTO createGrade(GradeDTO gradeDTO) {
        Grade grade = convertToEntity(gradeDTO);
        
        // Если указан scheduleId, автоматически заполняем subject и groupId из расписания
        if (gradeDTO.getScheduleId() != null && !gradeDTO.getScheduleId().isEmpty()) {
            scheduleRepository.findByScheduleId(gradeDTO.getScheduleId()).ifPresent(schedule -> {
                if (grade.getSubject() == null || grade.getSubject().isEmpty()) {
                    grade.setSubject(schedule.getSubject());
                }
                if (grade.getGroupId() == null || grade.getGroupId().isEmpty()) {
                    grade.setGroupId(schedule.getGroupId());
                }
            });
        }
        
        Grade savedGrade = gradeRepository.save(grade);
        return convertToDTO(savedGrade);
    }
    
    public Optional<GradeDTO> updateGrade(Long id, GradeDTO gradeDTO) {
        return gradeRepository.findById(id)
                .map(existingGrade -> {
                    existingGrade.setStudentId(gradeDTO.getStudentId());
                    existingGrade.setSubject(gradeDTO.getSubject());
                    existingGrade.setGradeValue(gradeDTO.getGradeValue());
                    existingGrade.setGradeType(gradeDTO.getGradeType());
                    existingGrade.setTeacherId(gradeDTO.getTeacherId());
                    existingGrade.setLessonDate(gradeDTO.getLessonDate());
                    existingGrade.setComment(gradeDTO.getComment());
                    existingGrade.setGroupId(gradeDTO.getGroupId());
                    existingGrade.setScheduleId(gradeDTO.getScheduleId());
                    Grade updatedGrade = gradeRepository.save(existingGrade);
                    return convertToDTO(updatedGrade);
                });
    }
    
    public boolean deleteGrade(Long id) {
        if (gradeRepository.existsById(id)) {
            gradeRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    /**
     * Получить статистику оценок студента по всем предметам
     */
    public StudentGradeStatsDTO getStudentGradeStats(String studentId) {
        StudentGradeStatsDTO stats = new StudentGradeStatsDTO();
        stats.setStudentId(studentId);
        
        // Получить имя студента
        userService.getUserById(studentId).ifPresent(user -> {
            stats.setStudentName(user.getFullName());
        });
        
        // Получить средний балл по всем предметам
        Double overallAverage = gradeRepository.getOverallAverageGradeByStudent(studentId);
        stats.setOverallAverage(overallAverage != null ? overallAverage : 0.0);
        
        // Получить все оценки студента
        List<GradeDTO> allGrades = getGradesByStudentId(studentId);
        stats.setTotalGrades(allGrades.size());
        
        // Получить список уникальных предметов
        List<String> subjects = gradeRepository.findDistinctSubjectsByStudentId(studentId);
        
        // Создать статистику по каждому предмету
        List<StudentGradeStatsDTO.SubjectGradeStatsDTO> subjectStatsList = new ArrayList<>();
        
        for (String subject : subjects) {
            StudentGradeStatsDTO.SubjectGradeStatsDTO subjectStats = new StudentGradeStatsDTO.SubjectGradeStatsDTO();
            subjectStats.setSubject(subject);
            
            // Получить оценки по предмету
            List<GradeDTO> subjectGrades = getGradesByStudentAndSubject(studentId, subject);
            subjectStats.setGrades(subjectGrades);
            subjectStats.setGradeCount(subjectGrades.size());
            
            // Вычислить средний балл по предмету
            Double average = gradeRepository.getAverageGradeByStudentAndSubject(studentId, subject);
            subjectStats.setAverageGrade(average != null ? average : 0.0);
            
            // Найти минимальную и максимальную оценку
            if (!subjectGrades.isEmpty()) {
                Integer minGrade = subjectGrades.stream()
                        .mapToInt(GradeDTO::getGradeValue)
                        .min()
                        .orElse(0);
                Integer maxGrade = subjectGrades.stream()
                        .mapToInt(GradeDTO::getGradeValue)
                        .max()
                        .orElse(0);
                subjectStats.setMinGrade(minGrade);
                subjectStats.setMaxGrade(maxGrade);
            }
            
            subjectStatsList.add(subjectStats);
        }
        
        stats.setSubjectStats(subjectStatsList);
        return stats;
    }
    
    /**
     * Получить средний балл студента по конкретному предмету
     */
    public Double getAverageGradeBySubject(String studentId, String subject) {
        return gradeRepository.getAverageGradeByStudentAndSubject(studentId, subject);
    }
    
    /**
     * Получить средний балл студента по всем предметам
     */
    public Double getOverallAverageGrade(String studentId) {
        return gradeRepository.getOverallAverageGradeByStudent(studentId);
    }
    
    private GradeDTO convertToDTO(Grade grade) {
        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setStudentId(grade.getStudentId());
        dto.setSubject(grade.getSubject());
        dto.setGradeValue(grade.getGradeValue());
        dto.setGradeType(grade.getGradeType());
        dto.setTeacherId(grade.getTeacherId());
        dto.setLessonDate(grade.getLessonDate());
        dto.setComment(grade.getComment());
        dto.setGroupId(grade.getGroupId());
        dto.setScheduleId(grade.getScheduleId());
        dto.setCreatedAt(grade.getCreatedAt());
        dto.setUpdatedAt(grade.getUpdatedAt());
        return dto;
    }
    
    private Grade convertToEntity(GradeDTO gradeDTO) {
        Grade grade = new Grade();
        grade.setStudentId(gradeDTO.getStudentId());
        grade.setSubject(gradeDTO.getSubject());
        grade.setGradeValue(gradeDTO.getGradeValue());
        grade.setGradeType(gradeDTO.getGradeType());
        grade.setTeacherId(gradeDTO.getTeacherId());
        grade.setLessonDate(gradeDTO.getLessonDate());
        grade.setComment(gradeDTO.getComment());
        grade.setGroupId(gradeDTO.getGroupId());
        grade.setScheduleId(gradeDTO.getScheduleId());
        return grade;
    }
}

