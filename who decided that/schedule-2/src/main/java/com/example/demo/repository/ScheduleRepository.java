package com.example.demo.repository;

import com.example.demo.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    
    Optional<Schedule> findByScheduleId(String scheduleId);
    
    List<Schedule> findByScheduleNameContainingIgnoreCase(String scheduleName);
    
    @Query("SELECT s FROM Schedule s WHERE s.scheduleName LIKE %:name%")
    List<Schedule> findSchedulesByName(@Param("name") String name);
    
    @Query("SELECT s FROM Schedule s LEFT JOIN FETCH s.groups WHERE s.scheduleId = :scheduleId")
    Optional<Schedule> findByIdWithGroups(@Param("scheduleId") String scheduleId);
    
    // New methods for schedule management
    @Query("SELECT s FROM Schedule s WHERE s.groupId = :groupId")
    List<Schedule> findByGroupId(@Param("groupId") String groupId);
    
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.groupId = :groupId 
        AND s.dayOfWeek = UPPER(:dayOfWeek)
        AND s.groupId IS NOT NULL
    """)
    List<Schedule> findByGroupIdAndDayOfWeek(
            @Param("groupId") String groupId, 
            @Param("dayOfWeek") String dayOfWeek);
    
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.groupId = :groupId 
        AND s.dayOfWeek = :dayOfWeek
        AND (
            (s.startTime <= :startTime AND s.endTime > :startTime) OR
            (s.startTime < :endTime AND s.endTime >= :endTime) OR
            (s.startTime >= :startTime AND s.endTime <= :endTime)
        )
    """)
    List<Schedule> findConflictingSchedules(
            @Param("groupId") String groupId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime);
    
    @Query("""
        SELECT s FROM Schedule s 
        WHERE s.groupId = :groupId 
        AND s.dayOfWeek = :dayOfWeek
        AND s.scheduleId != :excludeId
        AND (
            (s.startTime <= :startTime AND s.endTime > :startTime) OR
            (s.startTime < :endTime AND s.endTime >= :endTime) OR
            (s.startTime >= :startTime AND s.endTime <= :endTime)
        )
    """)
    List<Schedule> findConflictingSchedulesExcludingId(
            @Param("groupId") String groupId,
            @Param("dayOfWeek") String dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") String excludeId);
    
    @Query("SELECT s FROM Schedule s WHERE LOWER(s.subject) LIKE LOWER(concat('%', :query, '%'))")
    List<Schedule> searchSchedules(@Param("query") String query);
    
    Page<Schedule> findBySubjectContainingIgnoreCase(String subject, Pageable pageable);
}
