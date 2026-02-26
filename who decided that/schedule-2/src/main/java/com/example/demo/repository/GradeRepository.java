package com.example.demo.repository;

import com.example.demo.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    
    List<Grade> findByStudentId(String studentId);
    
    List<Grade> findBySubject(String subject);
    
    List<Grade> findByGroupId(String groupId);
    
    List<Grade> findByStudentIdAndSubject(String studentId, String subject);
    
    List<Grade> findByStudentIdAndGroupId(String studentId, String groupId);
    
    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId AND g.lessonDate BETWEEN :startDate AND :endDate")
    List<Grade> findByStudentIdAndDateRange(@Param("studentId") String studentId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT g FROM Grade g WHERE g.groupId = :groupId AND g.subject = :subject ORDER BY g.lessonDate DESC")
    List<Grade> findByGroupIdAndSubject(@Param("groupId") String groupId, @Param("subject") String subject);
    
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.studentId = :studentId AND g.subject = :subject")
    Double getAverageGradeByStudentAndSubject(@Param("studentId") String studentId, @Param("subject") String subject);
    
    @Query("SELECT AVG(g.gradeValue) FROM Grade g WHERE g.studentId = :studentId")
    Double getOverallAverageGradeByStudent(@Param("studentId") String studentId);
    
    @Query("SELECT DISTINCT g.subject FROM Grade g WHERE g.studentId = :studentId")
    List<String> findDistinctSubjectsByStudentId(@Param("studentId") String studentId);
    
    // Поиск оценок по расписанию
    List<Grade> findByScheduleId(String scheduleId);
    
    @Query("SELECT g FROM Grade g WHERE g.studentId = :studentId AND g.scheduleId = :scheduleId")
    List<Grade> findByStudentIdAndScheduleId(@Param("studentId") String studentId, 
                                              @Param("scheduleId") String scheduleId);
    
    @Query("SELECT g FROM Grade g WHERE g.scheduleId = :scheduleId AND g.subject = :subject")
    List<Grade> findByScheduleIdAndSubject(@Param("scheduleId") String scheduleId, 
                                           @Param("subject") String subject);

    /** Оценки группы за указанную дату (по полю lesson_date). */
    @Query("SELECT g FROM Grade g WHERE g.groupId = :groupId AND g.lessonDate >= :start AND g.lessonDate < :end ORDER BY g.subject, g.lessonDate")
    List<Grade> findByGroupIdAndLessonDateBetween(
            @Param("groupId") String groupId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}

