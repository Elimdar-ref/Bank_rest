package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.CreateCardRequest;
import com.example.bankcards.service.CardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final CardService cardService;

    @GetMapping("/cards")
    public ResponseEntity<Page<CardDTO>> getAllCards(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<CardDTO> cards = cardService.getAllCards(pageable);
        return ResponseEntity.ok(cards);
    }

    @PostMapping("/users/{userId}/cards")
    public ResponseEntity<CardDTO> createCardForUser(@PathVariable Long userId,
                                                     @Valid @RequestBody CreateCardRequest request) {
        CardDTO newCard = cardService.createCard(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(newCard);
    }

    @PutMapping("/cards/{cardId}/activate")
    public ResponseEntity<Map<String, String>> activateCard(@PathVariable Long cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok(Map.of("message", "Карта успешно активирована"));
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Map<String, String>> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok(Map.of("message", "Карта успешно удалена"));
    }
}