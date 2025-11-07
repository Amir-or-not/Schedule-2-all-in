package com.example.demo.service;

import com.example.demo.dto.AttendanceDTO;
import com.example.demo.entity.Attendance;
import com.example.demo.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRepository attendanceRepository;
    
    public List<AttendanceDTO> getAllAttendance() {
        return attendanceRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<AttendanceDTO> getAttendanceById(Long id) {
        return attendanceRepository.findById(id)
                .map(this::convertToDTO);
    }
    
    public List<AttendanceDTO> getAttendanceByStudentId(String studentId) {
        return attendanceRepository.findByStudentId(studentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceDTO> getAttendanceByGroupId(String groupId) {
        return attendanceRepository.findByGroupId(groupId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<AttendanceDTO> getAttendanceByDate(LocalDate date) {
        return attendanceRepository.findByAttendanceDate(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public AttendanceDTO createAttendance(AttendanceDTO attendanceDTO) {
        Attendance attendance = convertToEntity(attendanceDTO);
        Attendance savedAttendance = attendanceRepository.save(attendance);
        return convertToDTO(savedAttendance);
    }
    
    public Optional<AttendanceDTO> updateAttendance(Long id, AttendanceDTO attendanceDTO) {
        return attendanceRepository.findById(id)
                .map(existingAttendance -> {
                    existingAttendance.setStudentId(attendanceDTO.getStudentId());
                    existingAttendance.setSubject(attendanceDTO.getSubject());
                    existingAttendance.setAttendanceDate(attendanceDTO.getAttendanceDate());
                    existingAttendance.setStatus(attendanceDTO.getStatus());
                    existingAttendance.setTeacherId(attendanceDTO.getTeacherId());
                    existingAttendance.setGroupId(attendanceDTO.getGroupId());
                    existingAttendance.setComment(attendanceDTO.getComment());
                    Attendance updatedAttendance = attendanceRepository.save(existingAttendance);
                    return convertToDTO(updatedAttendance);
                });
    }
    
    public boolean deleteAttendance(Long id) {
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private AttendanceDTO convertToDTO(Attendance attendance) {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setId(attendance.getId());
        dto.setStudentId(attendance.getStudentId());
        dto.setSubject(attendance.getSubject());
        dto.setAttendanceDate(attendance.getAttendanceDate());
        dto.setStatus(attendance.getStatus());
        dto.setTeacherId(attendance.getTeacherId());
        dto.setGroupId(attendance.getGroupId());
        dto.setComment(attendance.getComment());
        dto.setCreatedAt(attendance.getCreatedAt());
        dto.setUpdatedAt(attendance.getUpdatedAt());
        return dto;
    }
    
    private Attendance convertToEntity(AttendanceDTO attendanceDTO) {
        Attendance attendance = new Attendance();
        attendance.setStudentId(attendanceDTO.getStudentId());
        attendance.setSubject(attendanceDTO.getSubject());
        attendance.setAttendanceDate(attendanceDTO.getAttendanceDate());
        attendance.setStatus(attendanceDTO.getStatus());
        attendance.setTeacherId(attendanceDTO.getTeacherId());
        attendance.setGroupId(attendanceDTO.getGroupId());
        attendance.setComment(attendanceDTO.getComment());
        return attendance;
    }
}

