package com.example.bankcards.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardResponseDTO {

    private Long id;

    private String maskedNumber;

    private String cardHolderName;

    private String ownerName;

    private String ownerUsername;

    private String expireDate;

    private String status;

    private BigDecimal balance;
}