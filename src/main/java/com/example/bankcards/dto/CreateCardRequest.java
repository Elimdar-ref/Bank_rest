package com.example.bankcards.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCardRequest {

    @NotBlank(message = "Неоходимо указать имя владельца карты")
    private String cardHolderName;
}