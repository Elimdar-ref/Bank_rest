package com.example.bankcards.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Имя пользователя обязательно")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Требуется пароль")
    @Size(min = 6)
    private String password;
    private String confirmPassword;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат почты")
    private String email;

    @NotBlank(message = "Требуется указать полное имя")
    private String fullName;
}