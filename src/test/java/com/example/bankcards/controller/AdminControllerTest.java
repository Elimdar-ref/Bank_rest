package com.example.bankcards.controller;

import com.example.bankcards.config.TestSecurityConfig;
import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.service.CardService;
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
@DisplayName("AdminController Тесты")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    private CardDTO cardDTO;
    private CreateCardRequest createCardRequest;

    @BeforeEach
    void setUp() {
        cardDTO = new CardDTO();
        cardDTO.setId(1L);
        cardDTO.setMaskedNumber("**** **** **** 1234");
        cardDTO.setCardHolderName("Test User");
        cardDTO.setExpiryDate("2028-12-31");
        cardDTO.setStatus("ACTIVE");
        cardDTO.setBalance(BigDecimal.ZERO);

        createCardRequest = new CreateCardRequest();
        createCardRequest.setCardHolderName("Test User");
    }

    @Test
    @DisplayName("GET /api/admin/cards - получение всех карт (админ)")
    void getAllCards_ShouldReturnPageOfCards() throws Exception {
        Page<CardDTO> cardPage = new PageImpl<>(List.of(cardDTO), PageRequest.of(0, 20), 1);
        when(cardService.getAllCards(any())).thenReturn(cardPage);

        mockMvc.perform(get("/api/admin/cards")
                        .param("page", "0")
                        .param("size", "20")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(cardService). getAllCards(any());
    }

    @Test
    @DisplayName("POST /api/admin/users/{userId}/cards - создание карты (админ)")
    void createCardForUser_ShouldReturnCreatedCard() throws Exception {
        when(cardService.createCard(eq(1L), any(CreateCardRequest.class))).thenReturn(cardDTO);

        mockMvc.perform(post("/api/admin/users/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardRequest))
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardHolderName").value("Test User"));

        verify(cardService).createCard(eq(1L), any(CreateCardRequest.class));
    }

    @Test
    @DisplayName("POST /api/admin/users/{userId}/cards - с пустым именем - ошибка валидации")
    void createCardForUser_WithEmptyName_ShouldReturnBadRequest() throws Exception {
        createCardRequest.setCardHolderName("");

        mockMvc.perform(post("/api/admin/users/1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createCardRequest))
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.cardHolderName").exists());

        verify(cardService, never()).createCard(any(), any());
    }

    @Test
    @DisplayName("PUT /api/admin/cards/{cardId}/activate - активация карты (админ)")
    void activateCard_ShouldReturnSuccess() throws Exception {
        doNothing().when(cardService).activateCard(1L);

        mockMvc.perform(put("/api/admin/cards/1/activate")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Карта успешно активирована"));

        verify(cardService).activateCard(1L);
    }

    @Test
    @DisplayName("DELETE /api/admin/cards/{cardId} - удаление карты (админ)")
    void deleteCard_ShouldReturnSuccess() throws Exception {
        doNothing().when(cardService).deleteCard(1L);

        mockMvc.perform(delete("/api/admin/cards/1")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Карта успешно удалена"));

        verify(cardService).deleteCard(1L);
    }

    @Test
    @DisplayName("DELETE /api/admin/cards/{cardId} - удаление несуществующей карты")
    void deleteCard_WhenCardNotFound_ShouldReturnError() throws Exception {
        doThrow(new RuntimeException("Карта не найдена")).when(cardService).deleteCard(999L);

        mockMvc.perform(delete("/api/admin/cards/999")
                        .header("Authorization", "Bearer admin-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Карта не найдена"));

        verify(cardService).deleteCard(999L);
    }
}