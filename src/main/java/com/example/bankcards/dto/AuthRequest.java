package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    private String username;

    @NotBlank(message = "Требуется пароль")
    private String password;
}