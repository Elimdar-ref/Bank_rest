package com.example.bankcards.controller;

import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestSecurityConfig.class)
@DisplayName("CardController Тесты")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private TransferService transferService;

    private CardDTO cardDTO;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        cardDTO = new CardDTO();
        cardDTO.setId(1L);
        cardDTO.setMaskedNumber("**** **** **** 1234");
        cardDTO.setCardHolderName("Test User");
        cardDTO.setExpiryDate("2028-12-31");
        cardDTO.setStatus("ACTIVE");
        cardDTO.setBalance(BigDecimal.valueOf(1000));

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("GET /api/cards - получение своих карт")
    void getMyCards_ShouldReturnPageOfCards() throws Exception {
        Page<CardDTO> cardPage = new PageImpl<>(List.of(cardDTO), PageRequest.of(0, 10), 1);
        when(cardService.getMyCards(any())).thenReturn(cardPage);

        mockMvc.perform(get("/api/cards")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].maskedNumber").value("**** **** **** 1234"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cardService).getMyCards(any());
    }

    @Test
    @DisplayName("GET /api/cards/{cardId}/balance - получение баланса карты")
    void getBalance_ShouldReturnBalance() throws Exception {
        when(cardService.getCardBalance(1L)).thenReturn(BigDecimal.valueOf(1000));

        mockMvc.perform(get("/api/cards/1/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000));

        verify(cardService).getCardBalance(1L);
    }

    @Test
    @DisplayName("GET /api/cards/total-balance - получение общего баланса")
    void getTotalBalance_ShouldReturnTotalBalance() throws Exception {
        when(transferService.getTotalBalance()).thenReturn(BigDecimal.valueOf(5000));

        mockMvc.perform(get("/api/cards/total-balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBalance").value(5000));

        verify(transferService).getTotalBalance();
    }

    @Test
    @DisplayName("PUT /api/cards/{cardId}/block - блокировка карты")
    void blockCard_ShouldReturnSuccess() throws Exception {
        doNothing().when(cardService).blockCard(1L);

        mockMvc.perform(put("/api/cards/1/block"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Карта успешно заблокирована"));

        verify(cardService).blockCard(1L);
    }

    @Test
    @DisplayName("POST /api/cards/transfer - перевод между картами")
    void transfer_ShouldReturnSuccess() throws Exception {
        doNothing().when(transferService).transfer(1L, 2L, BigDecimal.valueOf(100));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Перевод успешно завершен"));

        verify(transferService).transfer(1L, 2L, BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("POST /api/cards/transfer - с отрицательной суммой - ошибка валидации")
    void transfer_WithNegativeAmount_ShouldReturnBadRequest() throws Exception {
        transferRequest.setAmount(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());

        verify(transferService, never()).transfer(any(), any(), any());
    }

    @Test
    @DisplayName("POST /api/cards/transfer - с нулевой суммой - ошибка валидации")
    void transfer_WithZeroAmount_ShouldReturnBadRequest() throws Exception {
        transferRequest.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.amount").exists());

        verify(transferService, never()).transfer(any(), any(), any());
    }
}