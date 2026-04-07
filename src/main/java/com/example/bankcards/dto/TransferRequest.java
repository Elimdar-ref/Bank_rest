package com.example.bankcards.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "С какой карты переводим")
    private Long fromCardId;

    @NotNull(message = "На какую карту переводим")
    private Long toCardId;

    @NotNull(message = "Сумма перевода")
    @Positive(message = "Сумма должна быть положительной")
    private BigDecimal amount;
}