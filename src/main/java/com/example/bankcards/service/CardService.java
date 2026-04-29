package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CreateCardRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Map;

public interface CardService {

    CardDTO createCard(Long userId, CreateCardRequest request);

    Page<CardDTO> getMyCards(Pageable pageable);

    Page<CardDTO> getAllCards(Pageable pageable);

    void blockCard(Long cardId);

    void activateCard(Long cardId);

    void deleteCard(Long cardId);

    BigDecimal getCardBalance(Long cardId);
}