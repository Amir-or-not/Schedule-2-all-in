package com.example.demo.controller;

import com.example.demo.dto.GroupDTO;
import com.example.demo.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GroupController {
    
    private final GroupService groupService;
    private final ObjectMapper objectMapper;
    
    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }
    
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable("groupId") String groupId) {
        Optional<GroupDTO> group = groupService.getGroupById(groupId);
        return group.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/schedule/{scheduleId}")
    public ResponseEntity<List<GroupDTO>> getGroupsByScheduleId(@PathVariable("scheduleId") String scheduleId) {
        List<GroupDTO> groups = groupService.getGroupsByScheduleId(scheduleId);
        return ResponseEntity.ok(groups);
    }
    
    @PostMapping
    public ResponseEntity<?> createGroup(@Valid @RequestBody Map<String, Object> request) {
        try {
            log.info("Received group creation request: {}", request);
            
            // Validate required fields
            if (!request.containsKey("groupId") || request.get("groupId") == null) {
                return ResponseEntity.badRequest().body("Group ID is required");
            }
            
            // Create and populate DTO
            GroupDTO groupDTO = new GroupDTO();
            groupDTO.setGroupId(request.get("groupId").toString());
            
            // Set group name (required)
            if (request.containsKey("groupName") && request.get("groupName") != null) {
                groupDTO.setGroupName(request.get("groupName").toString());
            } else if (request.containsKey("name") && request.get("name") != null) {
                // For backward compatibility
                groupDTO.setGroupName(request.get("name").toString());
            } else {
                return ResponseEntity.badRequest().body("Group name is required");
            }
            
            // Set description (optional)
            if (request.containsKey("description") && request.get("description") != null) {
                groupDTO.setDescription(request.get("description").toString());
            }
            
            // Set schedule ID (optional, with default)
            if (request.containsKey("scheduleId") && request.get("scheduleId") != null) {
                groupDTO.setScheduleId(request.get("scheduleId").toString());
            } else {
                groupDTO.setScheduleId("default_schedule");
            }
            
            // Create the group
            GroupDTO createdGroup = groupService.createGroup(groupDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdGroup);
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error creating group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while creating the group: " + e.getMessage());
        }
    }
    
    @PutMapping("/{groupId}")
    public ResponseEntity<?> updateGroup(
            @PathVariable("groupId") String groupId,
            @RequestBody Map<String, Object> request) {
        try {
            log.info("Updating group {} with data: {}", groupId, request);
            
            // Get the existing group first
            GroupDTO existingGroup = groupService.getGroupById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
            
            // Update fields from request
            if (request.containsKey("groupName")) {
                existingGroup.setGroupName((String) request.get("groupName"));
            }
            if (request.containsKey("description")) {
                existingGroup.setDescription((String) request.get("description"));
            }
            if (request.containsKey("scheduleId")) {
                existingGroup.setScheduleId((String) request.get("scheduleId"));
            }
            
            // Handle the data field
            if (request.containsKey("data")) {
                Object data = request.get("data");
                if (data instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) data;
                    existingGroup.setData(dataMap);
                } else if (data != null) {
                    // If data is not a Map, try to convert it
                    String jsonData = objectMapper.writeValueAsString(data);
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) objectMapper.readValue(
                            jsonData, 
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
                    );
                    existingGroup.setData(dataMap);
                } else {
                    existingGroup.setData(new HashMap<>());
                }
            }
            
            GroupDTO updatedGroup = groupService.updateGroup(groupId, existingGroup)
                    .orElseThrow(() -> new RuntimeException("Failed to update group"));
                    
            return ResponseEntity.ok(updatedGroup);
            
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating the group: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("groupId") String groupId) {
        try {
            log.info("Deleting group with ID: {}", groupId);
            boolean deleted = groupService.deleteGroup(groupId);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting group: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<GroupDTO>> searchGroupsByName(@RequestParam("name") String name) {
        List<GroupDTO> groups = groupService.searchGroupsByName(name);
        return ResponseEntity.ok(groups);
    }
}
