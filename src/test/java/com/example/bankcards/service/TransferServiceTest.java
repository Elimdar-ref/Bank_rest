package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferService Тесты")
class TransferServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private TransferService transferService;

    private User testUser;
    private Card fromCard;
    private Card toCard;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole(Role.USER);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(testUser);
        fromCard.setStatus(Card.CardStatus.ACTIVE);
        fromCard.setBalance(BigDecimal.valueOf(1000));
        fromCard.setExpiryDate(LocalDate.now().plusYears(4));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(testUser);
        toCard.setStatus(Card.CardStatus.ACTIVE);
        toCard.setBalance(BigDecimal.valueOf(500));
        toCard.setExpiryDate(LocalDate.now().plusYears(4));
    }

    @Nested
    @DisplayName("Успешные переводы")
    class SuccessfulTransfers {

        @Test
        @DisplayName("Перевод между своими картами")
        void transferBetweenOwnCards() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
            when(securityUtils.getCurrentUserId()).thenReturn(1L);

            transferService.transfer(1L, 2L, BigDecimal.valueOf(200));

            assertThat(fromCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800));
            assertThat(toCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
            verify(cardRepository, times(2)).save(any(Card.class));
        }

        @Test
        @DisplayName("Перевод всех средств")
        void transferAllFunds() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
            when(securityUtils.getCurrentUserId()).thenReturn(1L);

            transferService.transfer(1L, 2L, BigDecimal.valueOf(1000));

            assertThat(fromCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(0));
            assertThat(toCard.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        }
    }

    @Nested
    @DisplayName("Ошибки перевода")
    class TransferErrors {

        @Test
        @DisplayName("Нельзя перевести на ту же карту")
        void cannotTransferToSameCard() {
            assertThatThrownBy(() -> transferService.transfer(1L, 1L, BigDecimal.valueOf(100)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Нельзя перевести деньги на ту же карту");
        }

        @Test
        @DisplayName("Нельзя перевести отрицательную сумму")
        void cannotTransferNegativeAmount() {
            assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.valueOf(-100)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("положительной");
        }

        @Test
        @DisplayName("Нельзя перевести ноль")
        void cannotTransferZero() {
            assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.ZERO))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("положительной");
        }

        @Test
        @DisplayName("Недостаточно средств")
        void insufficientFunds() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
            when(securityUtils.getCurrentUserId()).thenReturn(1L);

            assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.valueOf(2000)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Недостаточно средств");

            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Карта-отправитель не активна")
        void fromCardNotActive() {
            fromCard.setStatus(Card.CardStatus.BLOCKED);
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.of(toCard));
            when(securityUtils.getCurrentUserId()).thenReturn(1L);

            assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.valueOf(100)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("не активна");
        }

        @Test
        @DisplayName("Карта отправитель не найдена")
        void fromCardNotFound() {
            when(cardRepository.findById(1L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.valueOf(100)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("не найдена");
        }

        @Test
        @DisplayName("Карта получатель не найдена")
        void toCardNotFound() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(fromCard));
            when(cardRepository.findById(2L)).thenReturn(Optional.empty());
            when(securityUtils.getCurrentUserId()).thenReturn(1L);

            assertThatThrownBy(() -> transferService.transfer(1L, 2L, BigDecimal.valueOf(100)))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("не найдена");
        }
    }
}