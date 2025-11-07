package com.example.demo.repository;

import com.example.demo.entity.Homework;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HomeworkRepository extends JpaRepository<Homework, Long> {
    
    List<Homework> findByGroupId(String groupId);
    
    List<Homework> findByStudentId(String studentId);
    
    List<Homework> findBySubject(String subject);
    
    List<Homework> findByGroupIdAndSubject(String groupId, String subject);
    
    List<Homework> findByIsCompleted(Boolean isCompleted);
    
    List<Homework> findByStudentIdAndIsCompleted(String studentId, Boolean isCompleted);
    
    @Query("SELECT h FROM Homework h WHERE h.dueDate BETWEEN :startDate AND :endDate")
    List<Homework> findByDueDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT h FROM Homework h WHERE h.groupId = :groupId AND h.dueDate >= :currentDate ORDER BY h.dueDate ASC")
    List<Homework> findUpcomingByGroupId(@Param("groupId") String groupId, 
                                         @Param("currentDate") LocalDateTime currentDate);
}

