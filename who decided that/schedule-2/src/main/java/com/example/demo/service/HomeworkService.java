package com.example.demo.service;

import com.example.demo.dto.HomeworkDTO;
import com.example.demo.entity.Homework;
import com.example.demo.repository.HomeworkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class HomeworkService {
    
    @Autowired
    private HomeworkRepository homeworkRepository;
    
    public List<HomeworkDTO> getAllHomework() {
        return homeworkRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<HomeworkDTO> getHomeworkById(Long id) {
        return homeworkRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public List<HomeworkDTO> getHomeworkByGroupId(String groupId) {
        return homeworkRepository.findByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<HomeworkDTO> getHomeworkByStudentId(String studentId) {
        return homeworkRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<HomeworkDTO> getUpcomingHomeworkByGroupId(String groupId) {
        return homeworkRepository.findUpcomingByGroupId(groupId, LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public HomeworkDTO createHomework(HomeworkDTO homeworkDTO) {
        Homework homework = convertToEntity(homeworkDTO);
        if (homework.getAssignedDate() == null) {
            homework.setAssignedDate(LocalDateTime.now());
        }
        Homework savedHomework = homeworkRepository.save(homework);
        return convertToDTO(savedHomework);
    }
    
    public Optional<HomeworkDTO> updateHomework(Long id, HomeworkDTO homeworkDTO) {
        return homeworkRepository.findById(id)
                .map(existingHomework -> {
                    existingHomework.setTitle(homeworkDTO.getTitle());
                    existingHomework.setDescription(homeworkDTO.getDescription());
                    existingHomework.setSubject(homeworkDTO.getSubject());
                    existingHomework.setTeacherId(homeworkDTO.getTeacherId());
                    existingHomework.setGroupId(homeworkDTO.getGroupId());
                    existingHomework.setDueDate(homeworkDTO.getDueDate());
                    existingHomework.setIsCompleted(homeworkDTO.getIsCompleted());
                    existingHomework.setStudentId(homeworkDTO.getStudentId());
                    existingHomework.setAttachmentUrl(homeworkDTO.getAttachmentUrl());
                    Homework updatedHomework = homeworkRepository.save(existingHomework);
                    return convertToDTO(updatedHomework);
                });
    }
    
    public boolean deleteHomework(Long id) {
        if (homeworkRepository.existsById(id)) {
            homeworkRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private HomeworkDTO convertToDTO(Homework homework) {
        HomeworkDTO dto = new HomeworkDTO();
        dto.setId(homework.getId());
        dto.setTitle(homework.getTitle());
        dto.setDescription(homework.getDescription());
        dto.setSubject(homework.getSubject());
        dto.setTeacherId(homework.getTeacherId());
        dto.setGroupId(homework.getGroupId());
        dto.setDueDate(homework.getDueDate());
        dto.setAssignedDate(homework.getAssignedDate());
        dto.setIsCompleted(homework.getIsCompleted());
        dto.setStudentId(homework.getStudentId());
        dto.setAttachmentUrl(homework.getAttachmentUrl());
        dto.setCreatedAt(homework.getCreatedAt());
        dto.setUpdatedAt(homework.getUpdatedAt());
        return dto;
    }
    
    private Homework convertToEntity(HomeworkDTO homeworkDTO) {
        Homework homework = new Homework();
        homework.setTitle(homeworkDTO.getTitle());
        homework.setDescription(homeworkDTO.getDescription());
        homework.setSubject(homeworkDTO.getSubject());
        homework.setTeacherId(homeworkDTO.getTeacherId());
        homework.setGroupId(homeworkDTO.getGroupId());
        homework.setDueDate(homeworkDTO.getDueDate());
        homework.setAssignedDate(homeworkDTO.getAssignedDate());
        homework.setIsCompleted(homeworkDTO.getIsCompleted() != null ? homeworkDTO.getIsCompleted() : false);
        homework.setStudentId(homeworkDTO.getStudentId());
        homework.setAttachmentUrl(homeworkDTO.getAttachmentUrl());
        return homework;
    }
}

