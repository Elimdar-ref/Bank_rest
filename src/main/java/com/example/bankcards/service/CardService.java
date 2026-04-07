package com.example.bankcards.service;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardMaskingUtil;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.EncryptionUtil;
import com.example.bankcards.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final CardNumberGenerator cardNumberGenerator;
    private final EncryptionUtil encryptionUtil;
    private final CardMaskingUtil cardMaskingUtil;
    private final SecurityUtils securityUtils;

    private void checkAdmin(String action) {
        if (!securityUtils.isAdmin()) {
            throw new AccessDeniedException("Для " + action + " требуются права администратора");
        }
    }

    private void checkAccess(Card card) {
        boolean isAdmin = securityUtils.isAdmin();
        boolean isOwner = card.getUser().getId().equals(securityUtils.getCurrentUserId());

        if (!isAdmin && !isOwner) {
            throw new AccessDeniedException("Нет доступа к карте ID: " + card.getId());
        }
    }

    @Transactional
    public CardDTO createCard(Long userId, CreateCardRequest request) {
        checkAdmin("создания карты");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        //Генерируем уникальный номер карты
        String cardNumber;
        boolean unique;
        do {
            cardNumber = cardNumberGenerator.generate();
            String encrypted = encryptionUtil.encrypt(cardNumber);
            unique = !cardRepository.existsByCardNumberEncrypted(encrypted);
        } while (!unique);

        //Шифруем номер для хранения в БД
        String encryptedNumber = encryptionUtil.encrypt(cardNumber);

        Card card = new Card();
        card.setUser(user);
        card.setCardNumberEncrypted(encryptedNumber);
        card.setCardHolderName(request.getCardHolderName());
        card.setExpiryDate(LocalDate.now().plusYears(4));
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setBalance(BigDecimal.ZERO);

        Card saved = cardRepository.save(card);

        return convertToDTO(saved);
    }

    public Page<CardDTO> getMyCards(Pageable pageable) {
        User user = securityUtils.getCurrentUser();
        return cardRepository.findByUser(user, pageable)
                .map(this::convertToDTO);
    }

    public Page<CardDTO> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Transactional
    public void blockCard(Long cardId) {
        Card card = findCard(cardId);
        checkAccess(card);

        if (card.getStatus() == Card.CardStatus.BLOCKED) {
            throw new IllegalStateException("Карта уже заблокирована");
        }

        card.setStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);
    }

    @Transactional
    public void activateCard(Long cardId) {
        checkAdmin("активации карты");

        Card card = findCard(cardId);
        card.setStatus(Card.CardStatus.ACTIVE);
        cardRepository.save(card);
    }

    @Transactional
    public void deleteCard(Long cardId) {
        checkAdmin("удаления карты");

        if (!cardRepository.existsById(cardId)) {
            throw new RuntimeException("Карта не найдена");
        }
        cardRepository.deleteById(cardId);
    }

    public BigDecimal getCardBalance(Long cardId) {
        Card card = findCard(cardId);
        checkAccess(card);
        return card.getBalance();
    }

    private Card findCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта не найдена"));
    }

    private CardDTO convertToDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());

        String decryptedNumber = encryptionUtil.decrypt(card.getCardNumberEncrypted());
        String masked = cardMaskingUtil.maskCardNumber(decryptedNumber);
        dto.setMaskedNumber(masked);

        dto.setCardHolderName(card.getCardHolderName());
        dto.setExpiryDate(card.getExpiryDate().toString());
        dto.setStatus(card.getStatus().name());
        dto.setBalance(card.getBalance());

        return dto;
    }
}