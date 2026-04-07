package com.example.bankcards.util;

import org.springframework.stereotype.Component;
@Component
public class CardMaskingUtil {

    public String maskCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return "****";
        }

        String last4 = cardNumber.replace("ENC_", "");
        last4 = last4.substring(Math.max(0, last4.length() - 4));
        return "**** **** **** " + last4;
    }
}