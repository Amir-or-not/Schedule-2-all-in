package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "grup")  // Match the actual database table name
@Getter
@Setter
public class Group implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "group_id", nullable = false, length = 50)
    private String groupId;
    
    @Column(name = "schedule_id", nullable = false, length = 20)
    private String scheduleId;
    
    @Column(name = "group_name", length = 255)
    private String groupName;
    
    @Column(columnDefinition = "text")
    private String description;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> data = new HashMap<>();
    
    @Transient
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", insertable = false, updatable = false)
    private Schedule schedule;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<User> users = new ArrayList<>();
    
    // Constructors
    public Group() {}
    
    public Group(String groupId, String scheduleId, String groupName, String description, Map<String, Object> data) {
        this.groupId = groupId;
        this.scheduleId = scheduleId;
        this.groupName = groupName;
        this.description = description;
        this.data = data != null ? data : new HashMap<>();
    }
    
    // Getters and Setters are handled by Lombok @Getter and @Setter
    
    @Override
    public String toString() {
        return "Group{" +
                "groupId='" + groupId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data != null ? data : new HashMap<>();
    }
    
    // Helper method to convert data to JSON string if needed
    public String getDataAsString() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return new ObjectMapper().writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize data to JSON", e);
        }
    }
    
    // Helper method to set data from JSON string
    public void setDataFromString(String json) {
        if (json == null || json.trim().isEmpty()) {
            this.data = new HashMap<>();
            return;
        }
        try {
            this.data = new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON data", e);
        }
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Schedule getSchedule() {
        return schedule;
    }
    
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }
    
    public List<User> getUsers() {
        return users;
    }
    
    public void setUsers(List<User> users) {
        this.users = users;
    }
}
