package com.example.demo.service;

import com.example.demo.dto.GroupDTO;
import com.example.demo.entity.Group;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupService {
    
    private final GroupRepository groupRepository;
    // ModelMapper is now instantiated directly where needed

    public List<GroupDTO> getAllGroups() {
        log.debug("getAllGroups()");
        List<GroupDTO> list = groupRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        log.info("getAllGroups() - returned {} groups", list.size());
        return list;
    }
    
    public Optional<GroupDTO> getGroupById(String groupId) {
        log.debug("getGroupById(groupId={})", groupId);
        return groupRepository.findById(groupId)
                .map(this::convertToDTO);
    }
    
    public List<GroupDTO> getGroupsByScheduleId(String scheduleId) {
        log.debug("getGroupsByScheduleId(scheduleId={})", scheduleId);
        return groupRepository.findByScheduleId(scheduleId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public GroupDTO createGroup(GroupDTO groupDTO) {
        log.info("createGroup: groupId={}, groupName={}", groupDTO.getGroupId(), groupDTO.getGroupName());
        
        if (groupDTO.getGroupId() != null && groupRepository.existsById(groupDTO.getGroupId())) {
            String error = "Group with ID " + groupDTO.getGroupId() + " already exists";
            log.error("createGroup failed: {}", error);
            throw new IllegalArgumentException(error);
        }
        
        if (groupDTO.getScheduleId() == null || groupDTO.getScheduleId().trim().isEmpty()) {
            log.debug("createGroup: setting default scheduleId");
            groupDTO.setScheduleId("default_schedule");
        }
        
        if (groupDTO.getGroupName() == null || groupDTO.getGroupName().trim().isEmpty()) {
            log.debug("createGroup: setting default groupName");
            groupDTO.setGroupName("Unnamed Group");
        }
        
        try {
            Group group = convertToEntity(groupDTO);
            group = groupRepository.save(group);
            log.info("createGroup success: groupId={}", group.getGroupId());
            return convertToDTO(group);
        } catch (Exception e) {
            log.error("createGroup failed: groupId={}", groupDTO.getGroupId(), e);
            throw new RuntimeException("Failed to create group: " + e.getMessage(), e);
        }
    }
    
    public Optional<GroupDTO> updateGroup(String groupId, GroupDTO groupDTO) {
        log.debug("updateGroup(groupId={})", groupId);
        return groupRepository.findById(groupId)
                .map(existingGroup -> {
                    if (groupDTO.getScheduleId() != null && !groupDTO.getScheduleId().equals(existingGroup.getScheduleId())) {
                        log.debug("updateGroup: scheduleId changed to {}", groupDTO.getScheduleId());
                    }
                    existingGroup.setScheduleId(groupDTO.getScheduleId());
                    existingGroup.setGroupName(groupDTO.getGroupName());
                    existingGroup.setDescription(groupDTO.getDescription());
                    existingGroup.setData(groupDTO.getData());
                    Group updatedGroup = groupRepository.save(existingGroup);
                    log.info("updateGroup success: groupId={}", groupId);
                    return convertToDTO(updatedGroup);
                });
    }
    
    public boolean deleteGroup(String groupId) {
        log.debug("deleteGroup(groupId={})", groupId);
        return groupRepository.findById(groupId).map(group -> {
            groupRepository.deleteById(groupId);
            log.info("deleteGroup success: groupId={}", groupId);
            return true;
        }).orElseGet(() -> {
            log.warn("deleteGroup: group not found, groupId={}", groupId);
            return false;
        });
    }
    
    public List<GroupDTO> searchGroupsByName(String name) {
        log.debug("searchGroupsByName(name={})", name);
        return groupRepository.findByGroupNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private GroupDTO convertToDTO(Group group) {
        if (group == null) {
            return null;
        }
        GroupDTO dto = new GroupDTO();
        dto.setGroupId(group.getGroupId());
        dto.setScheduleId(group.getScheduleId());
        dto.setGroupName(group.getGroupName());
        dto.setDescription(group.getDescription());
        dto.setData(group.getData());
        return dto;
    }
    
    private Group convertToEntity(GroupDTO dto) {
        if (dto == null) {
            return null;
        }
        Group group = new Group();
        group.setGroupId(dto.getGroupId());
        group.setScheduleId(dto.getScheduleId());
        group.setGroupName(dto.getGroupName());
        group.setDescription(dto.getDescription());
        
        // Ensure data is never null
        if (dto.getData() == null) {
            group.setData(new HashMap<>());
        } else {
            // Create a new map to avoid potential Hibernate issues
            group.setData(new HashMap<>(dto.getData()));
        }
        
        return group;
    }
}
