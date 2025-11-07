package com.example.demo.repository;

import com.example.demo.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    
    List<Attendance> findByStudentId(String studentId);
    
    List<Attendance> findByGroupId(String groupId);
    
    List<Attendance> findBySubject(String subject);
    
    List<Attendance> findByAttendanceDate(LocalDate date);
    
    List<Attendance> findByStudentIdAndSubject(String studentId, String subject);
    
    List<Attendance> findByStudentIdAndAttendanceDateBetween(String studentId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT a FROM Attendance a WHERE a.groupId = :groupId AND a.attendanceDate = :date")
    List<Attendance> findByGroupIdAndDate(@Param("groupId") String groupId, @Param("date") LocalDate date);
    
    @Query("SELECT a FROM Attendance a WHERE a.studentId = :studentId AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findByStudentIdAndDateRange(@Param("studentId") String studentId, 
                                                  @Param("startDate") LocalDate startDate, 
                                                  @Param("endDate") LocalDate endDate);
}

