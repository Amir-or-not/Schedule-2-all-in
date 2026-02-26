package com.example.demo.controller;

import com.example.demo.dto.AttendanceDTO;
import com.example.demo.dto.GradeDTO;
import com.example.demo.dto.GroupDTO;
import com.example.demo.dto.HomeworkDTO;
import com.example.demo.dto.ScheduleDTO;
import com.example.demo.dto.StudentGradeStatsDTO;
import com.example.demo.dto.UserDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.services.UserDetailsImpl;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.GradeService;
import com.example.demo.service.GroupService;
import com.example.demo.service.HomeworkService;
import com.example.demo.service.ScheduleService;
import com.example.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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

    @Autowired
    private UserService userService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private UserRepository userRepository;

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

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof UserDetailsImpl ud) {
            return ud.getId();
        }
        String email = auth.getName();
        return userRepository.findByEmail(email).map(User::getUserId).orElse(null);
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
     * Get class journal for a teacher: group info, students in group, grades for the group.
     * classId = groupId.
     */
    @GetMapping("/teacher/{teacherId}/class-journal")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Map<String, Object>> getClassJournal(
            @PathVariable String teacherId,
            @RequestParam String classId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> classJournal = new HashMap<>();
        classJournal.put("teacherId", teacherId);
        classJournal.put("groupId", classId);
        Optional<GroupDTO> groupOpt = groupService.getGroupById(classId);
        groupOpt.ifPresent(g -> {
            classJournal.put("groupName", g.getGroupName() != null ? g.getGroupName() : g.getGroupId());
        });
        List<UserDTO> students = userService.getUsersByGroupId(classId);
        classJournal.put("students", students);
        List<GradeDTO> grades = gradeService.getGradesByGroupId(classId);
        if ("TEACHER".equals(currentRole())) {
            String teacherSubject = currentUserSubject();
            if (teacherSubject != null && !teacherSubject.isBlank()) {
                grades = grades.stream().filter(g -> teacherSubject.equalsIgnoreCase(g.getSubject())).collect(Collectors.toList());
            }
        }
        classJournal.put("grades", grades);
        List<String> subjects = grades.stream()
                .map(GradeDTO::getSubject)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .collect(Collectors.toList());
        classJournal.put("subjects", subjects);
        return ResponseEntity.ok(classJournal);
    }

    /**
     * Журнал учителя по дате: расписание на день (по dayOfWeek) + оценки за эту дату по каждому уроку.
     * GET /api/diary/teacher/{teacherId}/journal?groupId=...&date=2025-02-26
     */
    @GetMapping("/teacher/{teacherId}/journal")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Map<String, Object>> getJournalByDate(
            @PathVariable String teacherId,
            @RequestParam String groupId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("getJournalByDate(teacherId={}, groupId={}, date={})", teacherId, groupId, date);
        Map<String, Object> journal = new HashMap<>();
        journal.put("date", date.toString());
        journal.put("dayOfWeek", date.getDayOfWeek().name());
        journal.put("groupId", groupId);
        journal.put("teacherId", teacherId);

        Optional<GroupDTO> groupOpt = groupService.getGroupById(groupId);
        groupOpt.ifPresent(g -> journal.put("groupName", g.getGroupName() != null ? g.getGroupName() : g.getGroupId()));

        List<UserDTO> students = userService.getUsersByGroupId(groupId);
        journal.put("students", students);

        List<ScheduleDTO> scheduleForDay = scheduleService.getScheduleByGroupIdAndDayOfWeek(groupId, date.getDayOfWeek().name());

        if ("TEACHER".equals(currentRole())) {
            String teacherSubject = currentUserSubject();
            if (teacherSubject != null && !teacherSubject.isBlank()) {
                scheduleForDay = scheduleForDay.stream()
                        .filter(s -> teacherSubject.equalsIgnoreCase(s.getSubject()))
                        .collect(Collectors.toList());
            }
        }

        List<GradeDTO> gradesForDate = gradeService.getGradesByGroupIdAndDate(groupId, date);
        List<AttendanceDTO> attendanceForDate = attendanceService.getAttendanceByGroupIdAndDate(groupId, date);

        List<Map<String, Object>> lessons = new ArrayList<>();
        for (ScheduleDTO lesson : scheduleForDay) {
            Map<String, Object> lessonEntry = new HashMap<>();
            lessonEntry.put("scheduleId", lesson.getScheduleId());
            lessonEntry.put("subject", lesson.getSubject());
            lessonEntry.put("startTime", lesson.getStartTime() != null ? lesson.getStartTime().toString() : null);
            lessonEntry.put("endTime", lesson.getEndTime() != null ? lesson.getEndTime().toString() : null);
            lessonEntry.put("teacher", lesson.getTeacher());
            List<Map<String, Object>> roster = new ArrayList<>();
            for (UserDTO student : students) {
                String sid = student.getUserId();
                Map<String, Object> row = new HashMap<>();
                row.put("studentId", sid);
                row.put("studentName", student.getFullName());
                String lessonSubject = lesson.getSubject();
                GradeDTO gradeForStudent = gradesForDate.stream()
                        .filter(g -> sid.equals(g.getStudentId()) && (lessonSubject != null && lessonSubject.equals(g.getSubject())))
                        .findFirst()
                        .orElse(null);
                if (gradeForStudent != null) {
                    row.put("gradeId", gradeForStudent.getId());
                    row.put("gradeValue", gradeForStudent.getGradeValue());
                    row.put("gradeType", gradeForStudent.getGradeType());
                    row.put("comment", gradeForStudent.getComment());
                } else {
                    row.put("gradeId", null);
                    row.put("gradeValue", null);
                    row.put("gradeType", null);
                    row.put("comment", null);
                }
                AttendanceDTO att = attendanceForDate.stream()
                        .filter(a -> sid.equals(a.getStudentId()) && (lessonSubject != null && lessonSubject.equals(a.getSubject())))
                        .findFirst().orElse(null);
                row.put("attendanceStatus", att != null ? att.getStatus() : null);
                row.put("attendanceId", att != null ? att.getId() : null);
                roster.add(row);
            }
            lessonEntry.put("roster", roster);
            lessons.add(lessonEntry);
        }
        journal.put("lessons", lessons);
        log.info("getJournalByDate - groupId={}, date={}, lessons={}", groupId, date, lessons.size());
        return ResponseEntity.ok(journal);
    }
    
    // ========== GRADES (Оценки) ==========
    
    /**
     * Все оценки. Ученик — только свои, учитель/админ — все (с опц. фильтром ?groupId=...&subject=...).
     */
    @GetMapping("/grades")
    public ResponseEntity<List<GradeDTO>> getAllGrades(
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String subject) {
        List<GradeDTO> grades;
        String role = currentRole();
        if ("ADMIN".equals(role)) {
            grades = gradeService.getAllGrades();
            if (groupId != null && !groupId.isBlank()) {
                grades = grades.stream().filter(g -> groupId.equals(g.getGroupId())).collect(Collectors.toList());
            }
            if (subject != null && !subject.isBlank()) {
                grades = grades.stream().filter(g -> subject.equalsIgnoreCase(g.getSubject())).collect(Collectors.toList());
            }
        } else if ("TEACHER".equals(role)) {
            grades = gradeService.getAllGrades();
            String teacherSubject = currentUserSubject();
            if (teacherSubject != null && !teacherSubject.isBlank()) {
                grades = grades.stream().filter(g -> teacherSubject.equalsIgnoreCase(g.getSubject())).collect(Collectors.toList());
            }
            if (groupId != null && !groupId.isBlank()) {
                grades = grades.stream().filter(g -> groupId.equals(g.getGroupId())).collect(Collectors.toList());
            }
            if (subject != null && !subject.isBlank()) {
                grades = grades.stream().filter(g -> subject.equalsIgnoreCase(g.getSubject())).collect(Collectors.toList());
            }
        } else {
            String uid = currentUserId();
            grades = uid != null ? gradeService.getGradesByStudentId(uid) : List.of();
        }
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/{id}")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        Optional<GradeDTO> grade = gradeService.getGradeById(id);
        if (grade.isEmpty()) return ResponseEntity.notFound().build();
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(grade.get().getStudentId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(grade.get());
    }
    
    /**
     * Оценки по studentId. Ученик может запросить только свои.
     */
    @GetMapping("/grades/student/{studentId}")
    public ResponseEntity<?> getGradesByStudent(@PathVariable String studentId) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своим оценкам");
            }
        }
        List<GradeDTO> grades = gradeService.getGradesByStudentId(studentId);
        return ResponseEntity.ok(grades);
    }
    
    /** Оценки по группе — только учитель/админ. Учитель видит только свой предмет. */
    @GetMapping("/grades/group/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<GradeDTO>> getGradesByGroup(
            @PathVariable String groupId,
            @RequestParam(required = false) String subject) {
        List<GradeDTO> grades = gradeService.getGradesByGroupId(groupId);
        if ("TEACHER".equals(currentRole())) {
            String teacherSubject = currentUserSubject();
            if (teacherSubject != null && !teacherSubject.isBlank()) {
                grades = grades.stream().filter(g -> teacherSubject.equalsIgnoreCase(g.getSubject())).collect(Collectors.toList());
            }
        }
        if (subject != null && !subject.isBlank()) {
            grades = grades.stream().filter(g -> subject.equalsIgnoreCase(g.getSubject())).collect(Collectors.toList());
        }
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/student/{studentId}/subject/{subject}")
    public ResponseEntity<?> getGradesByStudentAndSubject(
            @PathVariable String studentId, 
            @PathVariable String subject) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своим оценкам");
            }
        }
        List<GradeDTO> grades = gradeService.getGradesByStudentAndSubject(studentId, subject);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/schedule/{scheduleId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<GradeDTO>> getGradesBySchedule(@PathVariable String scheduleId) {
        List<GradeDTO> grades = gradeService.getGradesByScheduleId(scheduleId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/student/{studentId}/schedule/{scheduleId}")
    public ResponseEntity<?> getGradesByStudentAndSchedule(
            @PathVariable String studentId,
            @PathVariable String scheduleId) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своим оценкам");
            }
        }
        List<GradeDTO> grades = gradeService.getGradesByStudentAndSchedule(studentId, scheduleId);
        return ResponseEntity.ok(grades);
    }
    
    @GetMapping("/grades/student/{studentId}/stats")
    public ResponseEntity<?> getStudentGradeStats(@PathVariable String studentId) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своей статистике");
            }
        }
        StudentGradeStatsDTO stats = gradeService.getStudentGradeStats(studentId);
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/grades/student/{studentId}/average")
    public ResponseEntity<?> getStudentAverageGrade(@PathVariable String studentId) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своей статистике");
            }
        }
        Double average = gradeService.getOverallAverageGrade(studentId);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("studentId", studentId);
        response.put("averageGrade", average != null ? average : 0.0);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/grades/student/{studentId}/subject/{subject}/average")
    public ResponseEntity<?> getStudentAverageGradeBySubject(
            @PathVariable String studentId,
            @PathVariable String subject) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своей статистике");
            }
        }
        Double average = gradeService.getAverageGradeBySubject(studentId, subject);
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("studentId", studentId);
        response.put("subject", subject);
        response.put("averageGrade", average != null ? average : 0.0);
        return ResponseEntity.ok(response);
    }
    
    /** Создание оценки — только учитель/админ. Учитель может ставить только по своему предмету. */
    @PostMapping("/grades")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> createGrade(@Valid @RequestBody GradeDTO gradeDTO) {
        log.debug("createGrade(studentId={}, subject={}, value={})", gradeDTO.getStudentId(), gradeDTO.getSubject(), gradeDTO.getGradeValue());
        if ("TEACHER".equals(currentRole())) {
            String teacherSubject = currentUserSubject();
            if (teacherSubject != null && !teacherSubject.isBlank()
                    && !teacherSubject.equalsIgnoreCase(gradeDTO.getSubject())) {
                log.warn("createGrade - teacher subject mismatch: allowed={}, requested={}", teacherSubject, gradeDTO.getSubject());
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Вы можете ставить оценки только по предмету: " + teacherSubject);
            }
        }
        try {
            GradeDTO createdGrade = gradeService.createGrade(gradeDTO);
            log.info("createGrade - grade created: id={}, studentId={}, subject={}", createdGrade.getId(), createdGrade.getStudentId(), createdGrade.getSubject());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGrade);
        } catch (Exception e) {
            log.error("createGrade failed: studentId={}, subject={}", gradeDTO.getStudentId(), gradeDTO.getSubject(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /** Обновление оценки — только учитель/админ. Учитель может редактировать только свой предмет. */
    @PutMapping("/grades/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<?> updateGrade(@PathVariable Long id, 
                                                 @Valid @RequestBody GradeDTO gradeDTO) {
        if ("TEACHER".equals(currentRole())) {
            String teacherSubject = currentUserSubject();
            if (teacherSubject != null && !teacherSubject.isBlank()
                    && !teacherSubject.equalsIgnoreCase(gradeDTO.getSubject())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Вы можете редактировать оценки только по предмету: " + teacherSubject);
            }
        }
        try {
            Optional<GradeDTO> updatedGrade = gradeService.updateGrade(id, gradeDTO);
            if (updatedGrade.isPresent()) {
                log.info("updateGrade - grade updated: id={}", id);
            } else {
                log.warn("updateGrade - grade not found: id={}", id);
            }
            return updatedGrade.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("updateGrade failed: id={}", id, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /** Удаление оценки — только учитель/админ. */
    @DeleteMapping("/grades/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        boolean deleted = gradeService.deleteGrade(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // ========== HOMEWORK (Домашние задания) ==========
    
    @GetMapping("/homework")
    public ResponseEntity<List<HomeworkDTO>> getAllHomework() {
        List<HomeworkDTO> homework = homeworkService.getAllHomework();
        if (!isAdminOrTeacher()) {
            String groupId = currentUserGroupId();
            if (groupId != null) {
                homework = homework.stream()
                        .filter(h -> groupId.equals(h.getGroupId()))
                        .collect(Collectors.toList());
            }
        }
        return ResponseEntity.ok(homework);
    }
    
    @GetMapping("/homework/{id}")
    public ResponseEntity<HomeworkDTO> getHomeworkById(@PathVariable Long id) {
        Optional<HomeworkDTO> homework = homeworkService.getHomeworkById(id);
        return homework.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /** Домашка по группе. Ученик — только своя группа. */
    @GetMapping("/homework/group/{groupId}")
    public ResponseEntity<?> getHomeworkByGroup(@PathVariable String groupId) {
        if (!isAdminOrTeacher()) {
            String userGroup = currentUserGroupId();
            if (userGroup == null || !userGroup.equals(groupId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своей группе");
            }
        }
        List<HomeworkDTO> homework = homeworkService.getHomeworkByGroupId(groupId);
        return ResponseEntity.ok(homework);
    }
    
    @GetMapping("/homework/student/{studentId}")
    public ResponseEntity<?> getHomeworkByStudent(@PathVariable String studentId) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своим данным");
            }
        }
        List<HomeworkDTO> homework = homeworkService.getHomeworkByStudentId(studentId);
        return ResponseEntity.ok(homework);
    }
    
    @GetMapping("/homework/group/{groupId}/upcoming")
    public ResponseEntity<?> getUpcomingHomework(@PathVariable String groupId) {
        if (!isAdminOrTeacher()) {
            String userGroup = currentUserGroupId();
            if (userGroup == null || !userGroup.equals(groupId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своей группе");
            }
        }
        List<HomeworkDTO> homework = homeworkService.getUpcomingHomeworkByGroupId(groupId);
        return ResponseEntity.ok(homework);
    }
    
    @PostMapping("/homework")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<HomeworkDTO> createHomework(@Valid @RequestBody HomeworkDTO homeworkDTO) {
        try {
            HomeworkDTO createdHomework = homeworkService.createHomework(homeworkDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdHomework);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/homework/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteHomework(@PathVariable Long id) {
        boolean deleted = homeworkService.deleteHomework(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    // ========== ATTENDANCE (Посещаемость) ==========
    
    @GetMapping("/attendance")
    public ResponseEntity<List<AttendanceDTO>> getAllAttendance() {
        List<AttendanceDTO> attendance = attendanceService.getAllAttendance();
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid != null) {
                attendance = attendance.stream()
                        .filter(a -> uid.equals(a.getStudentId()))
                        .collect(Collectors.toList());
            }
        }
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/attendance/{id}")
    public ResponseEntity<AttendanceDTO> getAttendanceById(@PathVariable Long id) {
        Optional<AttendanceDTO> attendance = attendanceService.getAttendanceById(id);
        return attendance.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/attendance/student/{studentId}")
    public ResponseEntity<?> getAttendanceByStudent(@PathVariable String studentId) {
        if (!isAdminOrTeacher()) {
            String uid = currentUserId();
            if (uid == null || !uid.equals(studentId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Доступ только к своим данным");
            }
        }
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByStudentId(studentId);
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/attendance/group/{groupId}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByGroup(@PathVariable String groupId) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByGroupId(groupId);
        return ResponseEntity.ok(attendance);
    }
    
    @GetMapping("/attendance/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<AttendanceDTO> attendance = attendanceService.getAttendanceByDate(date);
        return ResponseEntity.ok(attendance);
    }

    @GetMapping("/attendance/group/{groupId}/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<AttendanceDTO>> getAttendanceByGroupAndDate(
            @PathVariable String groupId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByGroupIdAndDate(groupId, date));
    }

    @PostMapping("/attendance/batch")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<List<AttendanceDTO>> batchUpsertAttendance(@RequestBody List<AttendanceDTO> list) {
        List<AttendanceDTO> results = list.stream()
                .map(attendanceService::upsertAttendance)
                .collect(Collectors.toList());
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/attendance")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<AttendanceDTO> createAttendance(@Valid @RequestBody AttendanceDTO attendanceDTO) {
        try {
            AttendanceDTO createdAttendance = attendanceService.createAttendance(attendanceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdAttendance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/attendance/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
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
    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResponseEntity<Void> deleteAttendance(@PathVariable Long id) {
        boolean deleted = attendanceService.deleteAttendance(id);
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

