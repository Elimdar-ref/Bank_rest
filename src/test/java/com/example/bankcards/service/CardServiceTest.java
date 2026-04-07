package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CardService Тесты")
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardMaskingUtil cardMaskingUtil;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private CardService cardService;

    private User testUser;
    private Card testCard;
    private CreateCardRequest createRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@test.com");
        testUser.setFullName("Test User");
        testUser.setRole(Role.USER);

        testCard = new Card();
        testCard.setId(1L);
        testCard.setUser(testUser);
        testCard.setCardNumberEncrypted("encrypted_123");
        testCard.setCardHolderName("Test User");
        testCard.setExpiryDate(LocalDate.now().plusYears(4));
        testCard.setStatus(Card.CardStatus.ACTIVE);
        testCard.setBalance(BigDecimal.valueOf(1000));

        createRequest = new CreateCardRequest();
        createRequest.setCardHolderName("Test User");
    }

    @Nested
    @DisplayName("Создание карты")
    class CreateCardTests {

        @Test
        @DisplayName("Админ может создать карту пользователю")
        void adminCanCreateCardForUser() {
            when(securityUtils.isAdmin()).thenReturn(true);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(cardNumberGenerator.generate()).thenReturn("4123456789012345");
            when(encryptionUtil.encrypt(anyString())).thenReturn("encrypted_123");
            when(cardRepository.existsByCardNumberEncrypted(anyString())).thenReturn(false);
            when(cardRepository.save(any(Card.class))).thenReturn(testCard);
            when(encryptionUtil.decrypt(anyString())).thenReturn("4123456789012345");
            when(cardMaskingUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 2345");

            CardDTO result = cardService.createCard(1L, createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getCardHolderName()).isEqualTo("Test User");
            verify(cardRepository).save(any(Card.class));
        }

        @Test
        @DisplayName("Обычный пользователь НЕ может создать карту")
        void userCannotCreateCard() {
            when(securityUtils.isAdmin()).thenReturn(false);

            assertThatThrownBy(() -> cardService.createCard(1L, createRequest))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("требуются права администратора");

            verify(cardRepository, never()).save(any(Card.class));
        }
    }

    @Nested
    @DisplayName("Блокировка карты")
    class BlockCardTests {

        @Test
        @DisplayName("Владелец может заблокировать свою карту")
        void ownerCanBlockOwnCard() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(securityUtils.isAdmin()).thenReturn(false);
            when(securityUtils.getCurrentUserId()).thenReturn(1L);

            cardService.blockCard(1L);

            assertThat(testCard.getStatus()).isEqualTo(Card.CardStatus.BLOCKED);
            verify(cardRepository).save(testCard);
        }

        @Test
        @DisplayName("Админ может заблокировать любую карту")
        void adminCanBlockAnyCard() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(securityUtils.isAdmin()).thenReturn(true);

            cardService.blockCard(1L);

            assertThat(testCard.getStatus()).isEqualTo(Card.CardStatus.BLOCKED);
            verify(cardRepository).save(testCard);
        }

        @Test
        @DisplayName("Пользователь НЕ может заблокировать чужую карту")
        void userCannotBlockOthersCard() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(securityUtils.isAdmin()).thenReturn(false);
            when(securityUtils.getCurrentUserId()).thenReturn(2L);

            assertThatThrownBy(() -> cardService.blockCard(1L))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessageContaining("Нет доступа");

            verify(cardRepository, never()).save(any(Card.class));
        }

        @Test
        @DisplayName("Нельзя заблокировать уже заблокированную карту")
        void cannotBlockAlreadyBlockedCard() {
            testCard.setStatus(Card.CardStatus.BLOCKED);
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(securityUtils.isAdmin()).thenReturn(true);

            assertThatThrownBy(() -> cardService.blockCard(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Карта уже заблокирована");
        }
    }

    @Nested
    @DisplayName("Просмотр карт")
    class GetCardsTests {

        @Test
        @DisplayName("Пользователь видит только свои карты")
        void userSeesOnlyOwnCards() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Card> cardPage = new PageImpl<>(List.of(testCard));

            when(securityUtils.getCurrentUser()).thenReturn(testUser);
            when(cardRepository.findByUser(testUser, pageable)).thenReturn(cardPage);
            when(encryptionUtil.decrypt(anyString())).thenReturn("4123456789012345");
            when(cardMaskingUtil.maskCardNumber(anyString())).thenReturn("**** **** **** 2345");

            Page<CardDTO> result = cardService.getMyCards(pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(cardRepository).findByUser(testUser, pageable);
        }
    }

    @Nested
    @DisplayName("Баланс карты")
    class BalanceTests {

        @Test
        @DisplayName("Владелец может посмотреть баланс своей карты")
        void ownerCanSeeOwnCardBalance() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(securityUtils.isAdmin()).thenReturn(false);
            when(securityUtils.getCurrentUserId()).thenReturn(1L);

            BigDecimal balance = cardService.getCardBalance(1L);

            assertThat(balance).isEqualByComparingTo(BigDecimal.valueOf(1000));
        }

        @Test
        @DisplayName("Пользователь НЕ может посмотреть баланс чужой карты")
        void userCannotSeeOthersCardBalance() {
            when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
            when(securityUtils.isAdmin()).thenReturn(false);
            when(securityUtils.getCurrentUserId()).thenReturn(2L);

            assertThatThrownBy(() -> cardService.getCardBalance(1L))
                    .isInstanceOf(AccessDeniedException.class);
        }
    }
}