package com.example.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Запрос на частичное обновление пользователя (роль, группа и т.д.) — пароль не требуется. */
@Data
public class UpdateUserRequest {
    @Size(max = 255)
    private String fullName;

    @Email
    @Size(max = 255)
    private String email;

    @Size(max = 20)
    private String groupId;

    @Size(max = 20)
    private String role;

    @Size(max = 100)
    private String subject;
}
