package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "schedule")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schedule {

    @Id
    @Column(name = "schedule_id", length = 50, nullable = false)
    private String scheduleId;

    @Column(name = "schedule_name", length = 255, nullable = false)
    private String scheduleName;
    
    @Column(name = "group_id", length = 50, nullable = false)
    private String groupId;
    
    @Column(name = "day_of_week", length = 20, nullable = false)
    private String dayOfWeek;
    
    @Column(name = "start_time", nullable = false, columnDefinition = "TIME DEFAULT '09:00:00'")
    private LocalTime startTime = LocalTime.of(9, 0);
    
    @Column(name = "end_time", nullable = false, columnDefinition = "TIME DEFAULT '10:00:00'")
    private LocalTime endTime = LocalTime.of(10, 0);
    
    @Column(name = "subject", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'Default Subject'")
    private String subject = "Default Subject";
    
    @Column(name = "teacher", length = 255)
    private String teacher;
    
    @Column(name = "room", length = 50)
    private String room;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ✅ Список групп, связанных с расписанием
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Group> groups = new ArrayList<>();

    // No-args constructor
    public Schedule() {
        this.groups = new ArrayList<>();
    }

    // All-args constructor
    public Schedule(String scheduleId, String scheduleName, Map<String, Object> data) {
        this();
        this.scheduleId = scheduleId;
        this.scheduleName = scheduleName;
        this.data = data;
    }

    // --- Getters and Setters ---
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups != null ? groups : new ArrayList<>();
    }
    
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getDayOfWeek() {
        return dayOfWeek;
    }
    
    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    
    public LocalTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject;
    }
    
    public String getTeacher() {
        return teacher;
    }
    
    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }
    
    public String getRoom() {
        return room;
    }
    
    public void setRoom(String room) {
        this.room = room;
    }
}
