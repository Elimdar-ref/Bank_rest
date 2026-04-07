package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.SecurityUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final CardRepository cardRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public void transfer(Long fromCardId, Long toCardId, BigDecimal amount) {

        if (fromCardId.equals(toCardId)) {
            throw new RuntimeException("Нельзя перевести деньги на ту же карту");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Сумма перевода должна быть положительной");
        }

        Long currentUserId = securityUtils.getCurrentUserId();

        Card fromCard = findCardWithOwnerCheck(fromCardId, currentUserId, "отправителя");
        Card toCard = findCardWithOwnerCheck(toCardId, currentUserId, "получателя");

        if (fromCard.getStatus() != Card.CardStatus.ACTIVE) {
            throw new RuntimeException("Карта-отправитель не активна");
        }

        if (fromCard.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Недостаточно средств на карте. Доступно: " + fromCard.getBalance());
        }

        fromCard.setBalance(fromCard.getBalance().subtract(amount));
        toCard.setBalance(toCard.getBalance().add(amount));

        cardRepository.save(fromCard);
        cardRepository.save(toCard);
    }

    public BigDecimal getTotalBalance() {
        Long currentUserId = securityUtils.getCurrentUserId();
        return cardRepository.sumBalanceByUserId(currentUserId);
    }

    private Card findCardWithOwnerCheck(Long cardId, Long userId, String cardType) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Карта " + (cardType != null ? cardType + " " : "")
                        + "не найдена, ID: " + cardId));

        if (!card.getUser().getId().equals(userId)) {
            throw new RuntimeException("Карта " + (cardType != null ? cardType + " " : "")
                    + "не принадлежит текущему пользователю");
        }

        return card;
    }
}