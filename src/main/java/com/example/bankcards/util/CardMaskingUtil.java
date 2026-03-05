package com.example.bankcards.util;

public class CardMaskingUtil {
    private CardMaskingUtil() {}

    public static String maskCardNumber(String rawNumber) {
        if (rawNumber == null || rawNumber.length() < 4) {
            throw new IllegalArgumentException("Card number is invalid");
        }
        String last4 = rawNumber.substring(rawNumber.length() - 4);
        return "**** **** **** " + last4;
    }
}
