package com.example.demo.service;

import com.example.demo.dto.AttendanceDTO;
import com.example.demo.entity.Attendance;
import com.example.demo.repository.AttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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

    public List<AttendanceDTO> getAttendanceByGroupIdAndDate(String groupId, LocalDate date) {
        return attendanceRepository.findByGroupIdAndDate(groupId, date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public AttendanceDTO upsertAttendance(AttendanceDTO dto) {
        List<Attendance> existing = attendanceRepository.findByGroupIdAndDate(
                dto.getGroupId(), dto.getAttendanceDate());
        Attendance found = existing.stream()
                .filter(a -> a.getStudentId().equals(dto.getStudentId())
                        && a.getSubject().equals(dto.getSubject()))
                .findFirst().orElse(null);
        if (found != null) {
            found.setStatus(dto.getStatus());
            found.setTeacherId(dto.getTeacherId());
            found.setComment(dto.getComment());
            return convertToDTO(attendanceRepository.save(found));
        }
        return createAttendance(dto);
    }
    
    public AttendanceDTO createAttendance(AttendanceDTO attendanceDTO) {
        Attendance attendance = convertToEntity(attendanceDTO);
        Attendance savedAttendance = attendanceRepository.save(attendance);
        log.info("[DATA] Attendance created: id={}, studentId={}, date={}", savedAttendance.getId(), savedAttendance.getStudentId(), savedAttendance.getAttendanceDate());
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
                    log.info("[DATA] Attendance updated: id={}", id);
                    return convertToDTO(updatedAttendance);
                });
    }
    
    public boolean deleteAttendance(Long id) {
        if (attendanceRepository.existsById(id)) {
            attendanceRepository.deleteById(id);
            log.info("[DATA] Attendance deleted: id={}", id);
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

