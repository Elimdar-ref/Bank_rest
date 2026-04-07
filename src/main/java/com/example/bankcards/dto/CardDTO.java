package com.example.bankcards.dto;

import com.example.bankcards.entity.Card;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardDTO {

    private Long id;

    private String maskedNumber;

    private String cardHolderName;

    private String expiryDate;

    private String status;

    private BigDecimal balance;
}