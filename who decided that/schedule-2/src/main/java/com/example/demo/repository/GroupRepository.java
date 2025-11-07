package com.example.demo.repository;

import com.example.demo.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, String> {
    
    // Find group by ID
    @Override
    Optional<Group> findById(String groupId);
    
    // Check if group exists by ID
    boolean existsByGroupId(String groupId);
    
    // Find all groups with a specific schedule ID
    List<Group> findByScheduleId(String scheduleId);
    
    // Find group by name (case-insensitive)
    List<Group> findByGroupNameContainingIgnoreCase(String name);
    
    // Custom query to find groups with a specific name pattern
    @Query("SELECT g FROM Group g WHERE LOWER(g.groupName) LIKE LOWER(concat('%', :name, '%'))")
    List<Group> searchByName(@Param("name") String name);
    
    @Query("SELECT g FROM Group g WHERE g.scheduleId = :scheduleId")
    List<Group> findGroupsByScheduleId(@Param("scheduleId") String scheduleId);
    
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.users WHERE g.groupId = :groupId")
    Optional<Group> findByIdWithUsers(@Param("groupId") String groupId);
    
    @Query("SELECT g FROM Group g LEFT JOIN FETCH g.schedule WHERE g.groupId = :groupId")
    Optional<Group> findByIdWithSchedule(@Param("groupId") String groupId);
}
