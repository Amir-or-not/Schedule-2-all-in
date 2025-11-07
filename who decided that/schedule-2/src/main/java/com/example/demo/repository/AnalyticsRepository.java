package com.example.demo.repository;

import com.example.demo.entity.Analytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {
    
    List<Analytics> findByPromptContainingIgnoreCase(String prompt);
    
    List<Analytics> findByAnswerContainingIgnoreCase(String answer);
    
    @Query("SELECT a FROM Analytics a WHERE a.prompt LIKE %:searchTerm% OR a.answer LIKE %:searchTerm%")
    List<Analytics> findByPromptOrAnswerContaining(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT a FROM Analytics a WHERE a.createdAt >= :startDate AND a.createdAt <= :endDate")
    List<Analytics> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT a FROM Analytics a ORDER BY a.createdAt DESC")
    List<Analytics> findAllOrderByCreatedAtDesc();
}
