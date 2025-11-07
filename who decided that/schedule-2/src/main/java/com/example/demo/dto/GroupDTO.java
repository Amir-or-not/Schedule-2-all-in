package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupDTO {
    
    @NotBlank(message = "Group ID is required")
    @Size(max = 20, message = "Group ID must not exceed 20 characters")
    private String groupId;
    
    @NotBlank(message = "Schedule ID is required")
    @Size(max = 20, message = "Schedule ID must not exceed 20 characters")
    private String scheduleId;
    
    @Size(max = 255, message = "Group name must not exceed 255 characters")
    private String groupName;
    
    private String description;
    
    private Map<String, Object> data;
    
    // Constructors
    public GroupDTO() {}
    
    public GroupDTO(String groupId, String scheduleId, String groupName, String description, Map<String, Object> data) {
        this.groupId = groupId;
        this.scheduleId = scheduleId;
        this.groupName = groupName;
        this.description = description;
        this.data = data;
    }
    
    // Getters and Setters
    public String getGroupId() {
        return groupId;
    }
    
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }
    
    public String getScheduleId() {
        return scheduleId;
    }
    
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }
    
    public String getGroupName() {
        return groupName;
    }
    
    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
        this.data = data;
    }
    
    // Helper method to convert data to JSON string if needed
    public String getDataAsString() {
        if (data == null) {
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
            this.data = null;
            return;
        }
        try {
            this.data = new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse JSON data", e);
        }
    }
}
