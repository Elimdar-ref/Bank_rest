package com.example.bankcards.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CardNumberGenerator {

    private static final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder cardNumber = new StringBuilder("4");
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < 15; i++) {
            cardNumber.append(random.nextInt(10));
        }

        return cardNumber.toString();
    }
}