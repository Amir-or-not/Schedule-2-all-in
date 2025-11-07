package com.example.demo.controller;

import com.example.demo.dto.AttendanceDTO;
import com.example.demo.dto.GradeDTO;
import com.example.demo.dto.HomeworkDTO;
import com.example.demo.dto.StudentGradeStatsDTO;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.GradeService;
import com.example.demo.service.HomeworkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/diary")
@CrossOrigin(origins = "*")
public class DiaryController {
    
    @Autowired
    private GradeService gradeService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private HomeworkService homeworkService;
    
    // Student Diary Endpoints
    
    /**
     * Get student's daily schedule with grades and attendance
     */
    @GetMapping("/student/{studentId}/daily")
    public ResponseEntity<Map<String, Object>> getStudentDailyDiary(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> dailyDiary = new HashMap<>();
        
        // Get schedule for the day
        // Get grades for the day
        // Get attendance for the day
        // Get homework due for the day
        
        return ResponseEntity.ok(dailyDiary);
    }
    
    /**
     * Get student's weekly summary
     */
    @GetMapping("/student/{studentId}/weekly")
    public ResponseEntity<Map<String, Object>> getStudentWeeklySummary(
            @PathVariable String studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStartDate) {
        Map<String, Object> weeklySummary = new HashMap<>();
        // Implementation for weekly summary
        return ResponseEntity.ok(weeklySummary);
    }
    
    // Parent Diary Endpoints
    
    /**
     * Get children's grades for a parent
     */
    @GetMapping("/parent/{parentId}/children/grades")
    public ResponseEntity<List<Map<String, Object>>> getChildrenGrades(
            @PathVariable String parentId,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> childrenGrades = new ArrayList<>();
        // Implementation to get children's grades
        return ResponseEntity.ok(childrenGrades);
    }
    
    // Teacher Diary Endpoints
    
    /**
     * Get class journal for a teacher
     */
    @GetMapping("/teacher/{teacherId}/class-journal")
    public ResponseEntity<Map<String, Object>> getClassJournal(
            @PathVariable String teacherId,
            @RequestParam String classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> classJournal = new HashMap<>();
        // Implementation for class journal
        return ResponseEntity.ok(classJournal);
    }
    
    // ========== GRADES (Оценки) ==========
    
    @GetMapping("/grades")
    public ResponseEntity<List<GradeDTO>> getAllGrades() {
        List<GradeDTO> grades = gradeService.getAllGrades();
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        Optional<GradeDTO> grade = gradeService.getGradeById(id);
        return grade.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/grades/student/{studentId}")
    public ResponseEntity<List<GradeDTO>> getGradesByStudent(@PathVariable String studentId) {
        List<GradeDTO> grades = gradeService.getGradesByStudentId(studentId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/group/{groupId}")
    public ResponseEntity<List<GradeDTO>> getGradesByGroup(@PathVariable String groupId) {
        List<GradeDTO> grades = gradeService.getGradesByGroupId(groupId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/student/{studentId}/subject/{subject}")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentAndSubject(
            @PathVariable String studentId, 
            @PathVariable String subject) {
        List<GradeDTO> grades = gradeService.getGradesByStudentAndSubject(studentId, subject);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/schedule/{scheduleId}")
    public ResponseEntity<List<GradeDTO>> getGradesBySchedule(@PathVariable String scheduleId) {
        List<GradeDTO> grades = gradeService.getGradesByScheduleId(scheduleId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/student/{studentId}/schedule/{scheduleId}")
    public ResponseEntity<List<GradeDTO>> getGradesByStudentAndSchedule(
            @PathVariable String studentId,
            @PathVariable String scheduleId) {
        List<GradeDTO> grades = gradeService.getGradesByStudentAndSchedule(studentId, scheduleId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/student/{studentId}/stats")
    public ResponseEntity<StudentGradeStatsDTO> getStudentGradeStats(@PathVariable String studentId) {
        StudentGradeStatsDTO stats = gradeService.getStudentGradeStats(studentId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/grades/student/{studentId}/average")
    public ResponseEntity<Map<String, Object>> getStudentAverageGrade(@PathVariable String studentId) {
        Double average = gradeService.getOverallAverageGrade(studentId);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("studentId", studentId);
        response.put("averageGrade", average != null ? average : 0.0);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/grades/student/{studentId}/subject/{subject}/average")
    public ResponseEntity<Map<String, Object>> getStudentAverageGradeBySubject(
            @PathVariable String studentId,
            @PathVariable String subject) {
        Double average = gradeService.getAverageGradeBySubject(studentId, subject);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("studentId", studentId);
        response.put("subject", subject);
        response.put("averageGrade", average != null ? average : 0.0);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/grades")
    public ResponseEntity<GradeDTO> createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        try {
            GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/grades/{id}")
    public ResponseEntity<GradeDTO> updateGrade(@PathVariable Long id, 
                                                 @Valid @RequestBody GradeDTO gradeDTO) {
        try {
            Optional<GradeDTO> updatedGrade = gradeService.updateGrade(id, gradeDTO);
            return updatedGrade.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/grades/{id}")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        boolean deleted = gradeService.deleteGrade(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // ========== HOMEWORK (Домашние задания) ==========
    
    @GetMapping("/homework")
    public ResponseEntity<List<HomeworkDTO>> getAllHomework() {
        List<HomeworkDTO> homework = homeworkService.getAllHomework();
        return ResponseEntity.ok(homework);
    }
    
    @GetMapping("/homework/{id}")
    public ResponseEntity<HomeworkDTO> getHomeworkById(@PathVariable Long id) {
        Optional<HomeworkDTO> homework = homeworkService.getHomeworkById(id);
        return homework.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/homework/group/{groupId}")
    public ResponseEntity<List<HomeworkDTO>> getHomeworkByGroup(@PathVariable String groupId) {
        List<HomeworkDTO> homework = homeworkService.getHomeworkByGroupId(groupId);
        return ResponseEntity.ok(homework);
    }
    
    @GetMapping("/homework/student/{studentId}")
    public ResponseEntity<List<HomeworkDTO>> getHomeworkByStudent(@PathVariable String studentId) {
        List<HomeworkDTO> homework = homeworkService.getHomeworkByStudentId(studentId);
        return ResponseEntity.ok(homework);
    }
    
    @GetMapping("/homework/group/{groupId}/upcoming")
    public ResponseEntity<List<HomeworkDTO>> getUpcomingHomework(@PathVariable String groupId) {
        List<HomeworkDTO> homework = homeworkService.getUpcomingHomeworkByGroupId(groupId);
        return ResponseEntity.ok(homework);
    }
    
    @PostMapping("/homework")
    public ResponseEntity<HomeworkDTO> createHomework(@Valid @RequestBody HomeworkDTO homeworkDTO) {
        try {
            HomeworkDTO createdHomework = homeworkService.createHomework(homeworkDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdHomework);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/homework/{id}")
    public ResponseEntity<HomeworkDTO> updateHomework(@PathVariable Long id, 
                                                     @Valid @RequestBody HomeworkDTO homeworkDTO) {
        try {
            Optional<HomeworkDTO> updatedHomework = homeworkService.updateHomework(id, homeworkDTO);
            return updatedHomework.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/homework/{id}")
    public ResponseEntity<Void> deleteHomework(@PathVariable Long id) {
        boolean deleted = homeworkService.deleteHomework(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // ========== ATTENDANCE (Посещаемость) ==========
    
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceDTO>> getAllAttendance() {
        List<AttendanceDTO> attendance = attendanceService.getAllAttendance();
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/attendance/{id}")
    public ResponseEntity<AttendanceDTO> getAttendanceById(@PathVariable Long id) {
        Optional<AttendanceDTO> attendance = attendanceService.getAttendanceById(id);
        return attendance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/attendance/student/{studentId}")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByStudent(@PathVariable String studentId) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByStudentId(studentId);
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/attendance/group/{groupId}")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByGroup(@PathVariable String groupId) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByGroupId(groupId);
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/attendance/date/{date}")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(attendance);
    }
    
    @PostMapping("/attendance")
    public ResponseEntity<AttendanceDTO> createAttendance(@Valid @RequestBody AttendanceDTO attendanceDTO) {
        try {
            AttendanceDTO createdAttendance = attendanceService.createAttendance(attendanceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAttendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/attendance/{id}")
    public ResponseEntity<AttendanceDTO> updateAttendance(@PathVariable Long id, 
                                                          @Valid @RequestBody AttendanceDTO attendanceDTO) {
        try {
            Optional<AttendanceDTO> updatedAttendance = attendanceService.updateAttendance(id, attendanceDTO);
            return updatedAttendance.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/attendance/{id}")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        boolean deleted = attendanceService.deleteAttendance(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

