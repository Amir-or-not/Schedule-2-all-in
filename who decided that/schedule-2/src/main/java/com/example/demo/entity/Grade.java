package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "grades")
@Data
public class Grade {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "student_id", nullable = false, length = 50)
    private String studentId;
    
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;
    
    @Column(name = "grade_value", nullable = false)
    private Integer gradeValue; // 1-5 или 1-100
    
    @Column(name = "grade_type", length = 50)
    private String gradeType; // "exam", "test", "homework", "quiz", "final"
    
    @Column(name = "teacher_id", length = 50)
    private String teacherId;
    
    @Column(name = "lesson_date")
    private LocalDateTime lessonDate;
    
    @Column(name = "comment", columnDefinition = "text")
    private String comment;
    
    @Column(name = "group_id", length = 50)
    private String groupId;
    
    @Column(name = "schedule_id", length = 50)
    private String scheduleId; // Привязка к уроку в расписании
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Many-to-One relationship with User (student)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", insertable = false, updatable = false)
    private User student;
    
    // Many-to-One relationship with Schedule (lesson)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;
}

