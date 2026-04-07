package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.service.CardService;
import com.example.bankcards.service.TransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;
    private final TransferService transferService;

    @GetMapping
    public ResponseEntity<Page<CardDTO>> getMyCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CardDTO> cards = cardService.getMyCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{cardId}/balance")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@PathVariable Long cardId) {
        BigDecimal balance = cardService.getCardBalance(cardId);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @GetMapping("/total-balance")
    public ResponseEntity<Map<String, BigDecimal>> getTotalBalance() {
        BigDecimal totalBalance = transferService.getTotalBalance();
        return ResponseEntity.ok(Map.of("totalBalance", totalBalance));
    }

    @PutMapping("/{cardId}/block")
    public ResponseEntity<Map<String, String>> blockCard(@PathVariable Long cardId) {
        cardService.blockCard(cardId);
        return ResponseEntity.ok(Map.of("message", "Карта успешно заблокирована"));
    }

    @PostMapping("/transfer")
    public ResponseEntity<Map<String, String>> transfer(@Valid @RequestBody TransferRequest request) {
        transferService.transfer(request.getFromCardId(), request.getToCardId(), request.getAmount());
        return ResponseEntity.ok(Map.of("message", "Перевод успешно завершен"));
    }
}