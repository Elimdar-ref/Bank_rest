package com.example.bankcards.service;

import java.math.BigDecimal;

public interface TransferService {
    void transfer(Long fromCardId, Long toCardId, BigDecimal amount);

    BigDecimal getTotalBalance();
}