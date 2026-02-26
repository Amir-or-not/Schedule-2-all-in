package com.example.demo.controller;

import com.example.demo.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private ScheduleService scheduleService;

    /**
     * Автозаполнение расписания для всех групп (ПН–ПТ, по 5 уроков в день).
     * Доступно только пользователям с ролью ADMIN.
     */
    @PostMapping("/schedule/seed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> seedSchedule() {
        int created = scheduleService.seedScheduleForAllGroups();
        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("created", created);
        body.put("message", "Создано записей расписания: " + created);
        return ResponseEntity.ok(body);
    }
}
