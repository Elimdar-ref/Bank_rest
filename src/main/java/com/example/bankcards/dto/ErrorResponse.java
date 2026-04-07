package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {

    //Время ошибки
    private String timestamp;

    //HTTP статус
    private int status;

    //Тип ошибки
    private String error;

    //Детальное сообщение
    private String message;

    //URL на котором произошла ошибка
    private String path;
}